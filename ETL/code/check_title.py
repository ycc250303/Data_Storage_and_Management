import pandas as pd

def search_in_csv(file_path, search_query, search_column='Title', display_columns=['Title', 'Editions']):
    print(f"正在读取文件: {file_path} ...")
    
    try:
        # 读取 CSV 文件
        df = pd.read_csv(file_path, encoding='utf-8')
        
        if search_column not in df.columns:
            print(f"错误: 文件中未找到 '{search_column}' 列。现有的列有: {list(df.columns)}")
            return

        # 确保搜索列是字符串类型
        df[search_column] = df[search_column].astype(str)

        # 过滤包含指定字符串的数据 (case=False 确保不区分大小写)
        results = df[df[search_column].str.contains(search_query, case=False, na=False)]

        # 打印结果
        count = len(results)
        print(f"\n查询结果 (在列 '{search_column}' 中包含 '{search_query}', 不区分大小写):")
        print("-" * 50)
        
        if count > 0:
            # 过滤存在的显示列
            valid_display_cols = [col for col in display_columns if col in df.columns]
            
            # 设置打印选项以显示全部
            with pd.option_context('display.max_rows', None, 'display.max_columns', None, 'display.width', 1000, 'display.max_colwidth', None):
                print(results[valid_display_cols])
            print("-" * 50)
            print(f"总计找到: {count} 条数据。")
        else:
            print("未找到包含该字符串的数据。")

    except Exception as e:
        print(f"发生错误: {e}")

# --- 使用设置 ---
file_to_check = '../movie_info_name_merged.csv'      # 你的文件路径
string_to_find = 'Various'           # 你要查找的字符串
target_col = 'Directors'                      # 要检查的列名
show_cols = ['Directors'] # 要显示的列名

search_in_csv(file_to_check, string_to_find, search_column=target_col, display_columns=show_cols)
