import re
import csv
import pandas as pd

# 提取出评论数据集中的评论时间
def extractReviewTime(input_file_path, output_file_path):
    # 用于匹配 product/productId
    product_id_pattern = re.compile(r'product/productId:\s*(\S+)')

    # 用于匹配 review/time
    review_time_pattern = re.compile(r'review/time:\s*(\S+)')

    with open(input_file_path, 'r', encoding='iso-8859-1') as input_file, \
         open(output_file_path, 'w', newline='', encoding='utf-8') as output_file:

        csvwriter = csv.writer(output_file)
        csvwriter.writerow(['productID', 'reviewTime'])

        product_id_match = None
        review_time_match = None

        for line in input_file:
            line = line.strip()

            if product_id_match is None:
                product_id_match = product_id_pattern.search(line)

            if review_time_match is None:
                review_time_match = review_time_pattern.search(line)

            # 同时匹配到之后写入
            if product_id_match and review_time_match:
                product_id = product_id_match.group(1)
                review_time = review_time_match.group(1)

                csvwriter.writerow([product_id, review_time])

                # 重置匹配
                product_id_match = None
                review_time_match = None
        
    print(f"评论时间提取完成，保存到 {output_file_path}")


# 提取出每部电影最早的评论时间
def extractUniqueReviewTime(input_csv, output_csv):
    df = pd.read_csv(input_csv)

    # groupby 取最小 reviewTime（最早）
    result_df = df.groupby('productID', as_index=False)['reviewTime'].min()

    result_df.to_csv(output_csv, index=False)

    print(f"每部电影最早的评论时间提取完成，保存到 {output_csv}")


# 计算每部电影的评论数
def countReviewNum(input_csv, output_csv):
    df = pd.read_csv(input_csv)

    id_counts = df['productID'].value_counts().reset_index()

    id_counts.columns = ['productID', 'review_num']

    id_counts.to_csv(output_csv, index=False)

    print(f"每部电影的评论数计算完成，保存到 {output_csv}")


if __name__ == '__main__':
    input_file = '../../movies.txt'  
    output_file = '../all_review_time.csv'

    extractReviewTime(input_file, output_file)

    # 其他处理流程
    extractUniqueReviewTime(output_file, '../earliest_review_time.csv')
    countReviewNum(output_file, '../review_num.csv')
