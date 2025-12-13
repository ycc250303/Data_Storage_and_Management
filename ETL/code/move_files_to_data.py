#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
将ETL文件夹中所有后缀为.csv和.pkl的文件迁移到data文件夹
"""

import os
import shutil

def move_files_to_data():
    """
    将ETL文件夹中所有后缀为.csv和.pkl的文件迁移到data文件夹
    """
    # 当前ETL文件夹路径
    etl_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    
    # data文件夹路径
    data_dir = os.path.join(etl_dir, '../data')
    
    # 确保data文件夹存在
    if not os.path.exists(data_dir):
        os.makedirs(data_dir)
        print(f"创建目录: {data_dir}")
    
    # 需要移动的文件扩展名
    extensions = ['.csv', '.pkl']
    
    # 统计移动的文件数量
    moved_count = 0
    
    # 遍历ETL文件夹中的所有文件
    for file_name in os.listdir(etl_dir):
        # 检查文件扩展名
        for ext in extensions:
            if file_name.endswith(ext):
                source_path = os.path.join(etl_dir, file_name)
                target_path = os.path.join(data_dir, file_name)
                
                # 如果目标文件已存在，询问是否覆盖
                if os.path.exists(target_path):
                    print(f"文件已存在，跳过: {file_name}")
                    continue
                
                try:
                    # 移动文件
                    shutil.move(source_path, target_path)
                    print(f"移动文件: {file_name} -> data/{file_name}")
                    moved_count += 1
                except Exception as e:
                    print(f"移动文件失败 {file_name}: {e}")
                break
    
    print(f"\n完成! 共移动了 {moved_count} 个文件")

if __name__ == "__main__":
    move_files_to_data()
