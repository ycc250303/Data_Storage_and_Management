import pandas as pd

# def removeRedundantMarks():
#     print("开始去除多余引号...")

#     # 读取 CSV 文件
#     raw_data = pd.read_csv('../movie_info.csv', encoding='utf-8')

#     # 清除表中所有的双引号，确保处理的是字符串类型的列
#     for column in raw_data.columns:
#         if raw_data[column].dtype == object:  # 通常 object 类型包含字符串
#             raw_data[column] = raw_data[column].str.replace('\"', '')

#     # 将处理后的数据保存到新文件
#     raw_data.to_csv('../movie_info_marks_cleaned.csv', index=False, encoding='utf-8')

#     print("去除多余引号完成...")

import pandas as pd
import csv

import pandas as pd

import pandas as pd
import re

INPUT_FILE = '../movie_info.csv'
OUTPUT_FILE = '../movie_info_marks_cleaned.csv'

def clean_nested_quotes():
    print("开始清理多余嵌套引号...")

    # 1️⃣ 读取 CSV，保留字段外围引号
    df = pd.read_csv(INPUT_FILE, encoding='utf-8', dtype=str, quotechar='"')

    # 2️⃣ 遍历所有字符串列，清理多余嵌套引号
    for col in df.columns:
        # 使用正则替换 ""content"" -> "content"
        df[col] = df[col].apply(
            lambda x: re.sub(r'^""(.*)""$', r'"\1"', x) if isinstance(x, str) else x
        )

    # 3️⃣ 保存 CSV，保留必要的引号（QUOTE_MINIMAL）
    df.to_csv(OUTPUT_FILE, index=False, encoding='utf-8', quoting=1)  # quoting=csv.QUOTE_MINIMAL

    print(f"完成，已生成 {OUTPUT_FILE}")

if __name__ == "__main__":
    clean_nested_quotes()


