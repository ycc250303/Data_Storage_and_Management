import pandas as pd

# 清除表中所有的引号
def removeRedundantMarks():
    print("开始去除多余引号...")

    # 读取 CSV 文件
    raw_data = pd.read_csv('../movie_info.csv', encoding='utf-8')

    # 清除表中所有的双引号，确保处理的是字符串类型的列
    for column in raw_data.columns:
        if raw_data[column].dtype == object:  # 通常 object 类型包含字符串
            raw_data[column] = raw_data[column].str.replace('\"', '')

    # 将处理后的数据保存到新文件
    raw_data.to_csv('../movie_info_marks_cleaned.csv', index=False, encoding='utf-8')

    print("去除多余引号完成...")

removeRedundantMarks()