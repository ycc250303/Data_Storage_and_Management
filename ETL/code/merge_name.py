import pickle
import pandas as pd
import re
from collections import defaultdict
from rapidfuzz import fuzz
from tqdm import tqdm

# ================== 文件配置 ==================
INPUT_FILE = '../movie_info_marks_cleaned.csv'
OUTPUT_CSV_FILE = '../movie_info_name_merged.csv'
OUTPUT_NAME_SET = '../name_set.pkl'
OUTPUT_NAME_MAP = '../name_map.pkl'

# ================== 参数配置（20w数据安全值） ==================
FUZZY_THRESHOLD = 92     # 相似度阈值（不建议低于 90）
MIN_BUCKET_SIZE = 2      # 至少2个才做比较

# ================== 姓名规范化 ==================
def normalize(name: str) -> str:
    name = name.lower().strip()
    name = re.sub(r'[^a-z\s]', '', name)
    name = re.sub(r'\s+', ' ', name)
    return name

def name_key(norm_name: str):
    """
    分桶 key：姓 + 名首字母
    """
    parts = norm_name.split()
    if len(parts) == 0:
        return None
    last_name = parts[-1]
    first_initial = parts[0][0]
    return last_name, first_initial

# ================== Step 1：提取姓名 ==================
def beforeMerge():
    print("🚀 开始提取所有姓名...")
    df = pd.read_csv(INPUT_FILE, encoding='utf-8', low_memory=False)

    name_set = set()

    for col in ['Directors', 'Actors']:
        if col not in df.columns:
            continue

        for names in df[col].dropna():
            for name in str(names).split(','):
                name = name.strip()
                if name:
                    name_set.add(name)

    print(f"✅ 提取完成，唯一姓名数：{len(name_set)}")

    with open(OUTPUT_NAME_SET, 'wb') as f:
        pickle.dump(name_set, f)

    with open(OUTPUT_NAME_MAP, 'wb') as f:
        pickle.dump({}, f)

# ================== Step 2：分桶 + fuzzy 去重 ==================
def mergeNames():
    print("🚀 开始合并姓名（分桶 + rapidfuzz）...")

    with open(OUTPUT_NAME_SET, 'rb') as f:
        name_set = pickle.load(f)

    # 1️⃣ 规范化映射
    norm_map = defaultdict(list)
    for name in name_set:
        norm = normalize(name)
        if norm:
            norm_map[norm].append(name)

    # 2️⃣ 完全一致的直接合并
    name_mappings = {}
    canonical = {}

    for norm, originals in norm_map.items():
        main = originals[0]
        canonical[norm] = main
        for other in originals[1:]:
            name_mappings[other] = main

    # 3️⃣ 分桶
    buckets = defaultdict(list)
    for norm, main_name in canonical.items():
        key = name_key(norm)
        if key:
            buckets[key].append(main_name)

    # 4️⃣ 桶内 fuzzy
    for key, names in tqdm(buckets.items(), desc="🔍 分桶匹配"):
        if len(names) < MIN_BUCKET_SIZE:
            continue

        for i in range(len(names)):
            a = names[i]
            na = normalize(a)
            for j in range(i + 1, len(names)):
                b = names[j]
                if b in name_mappings:
                    continue

                nb = normalize(b)
                score = fuzz.ratio(na, nb)
                if score >= FUZZY_THRESHOLD:
                    name_mappings[b] = a

    print(f"✅ 合并完成，共生成映射关系：{len(name_mappings)}")

    with open(OUTPUT_NAME_MAP, 'wb') as f:
        pickle.dump(name_mappings, f)

# ================== Step 3：回填 CSV ==================
def afterMerge():
    print("🚀 开始替换 CSV 中姓名...")
    df = pd.read_csv(INPUT_FILE, encoding='utf-8', low_memory=False)

    with open(OUTPUT_NAME_MAP, 'rb') as f:
        name_mappings = pickle.load(f)

    def replace_names(cell):
        if pd.isna(cell):
            return cell
        result = []
        for name in str(cell).split(','):
            name = name.strip()
            result.append(name_mappings.get(name, name))
        return ', '.join(result)

    for col in ['Directors', 'Actors']:
        if col in df.columns:
            df[col] = df[col].apply(replace_names)
            print(f"✅ 已处理列：{col}")

    df.to_csv(OUTPUT_CSV_FILE, index=False, encoding='utf-8')
    print(f"🎉 完成！输出文件：{OUTPUT_CSV_FILE}")

# ================== 主入口 ==================
if __name__ == "__main__":
    beforeMerge()
    mergeNames()
    afterMerge()
