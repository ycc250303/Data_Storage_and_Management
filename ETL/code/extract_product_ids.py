#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
从 movies.txt 文件中提取 product/productId 字段并去重
使用流式处理，适合处理大文件
"""

def extract_unique_product_ids(input_file, output_file):
    """
    从输入文件中提取唯一的 product/productId 并写入输出文件
    
    Args:
        input_file: 输入文件路径
        output_file: 输出文件路径
    """
    product_ids = set()  # 使用 set 自动去重
    
    print(f"正在读取文件: {input_file}")
    
    # 使用 latin-1 编码，它可以解码任何字节序列（0-255），不会出现解码错误
    # 这对于包含混合编码的大文件非常有效
    try:
        with open(input_file, 'r', encoding='latin-1', errors='replace') as f:
            for line_num, line in enumerate(f, 1):
                line = line.strip()
                
                # 检查是否是 product/productId 行
                if line.startswith('product/productId:'):
                    # 提取 productId（冒号后的内容）
                    product_id = line.split(':', 1)[1].strip()
                    if product_id:  # 确保不为空
                        product_ids.add(product_id)
                
                # 每处理 100000 行输出一次进度
                if line_num % 100000 == 0:
                    print(f"已处理 {line_num} 行，当前唯一 productId 数量: {len(product_ids)}")
    
    except FileNotFoundError:
        print(f"错误: 找不到文件 {input_file}")
        return
    except Exception as e:
        print(f"读取文件时发生错误: {e}")
        return
    
    print(f"\n提取完成！共找到 {len(product_ids)} 个唯一的 productId")
    print(f"正在写入到文件: {output_file}")
    
    # 将去重后的 productId 写入输出文件
    try:
        with open(output_file, 'w', encoding='utf-8') as f:
            # 排序后写入，便于查看
            for product_id in sorted(product_ids):
                f.write(product_id + '\n')
        
        print(f"成功！已将所有唯一的 productId 写入 {output_file}")
        
    except Exception as e:
        print(f"写入文件时发生错误: {e}")


if __name__ == '__main__':
    input_file = '../../movies.txt'
    output_file = '../unique_product_ids.txt'
    
    extract_unique_product_ids(input_file, output_file)
