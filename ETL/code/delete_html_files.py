import os
import sys

def delete_html_files():
    """
    从unique_product_ids.txt的第19万行开始，检查results文件夹中是否存在同名的HTML文件，如果存在则删除
    """
    # 设置文件路径
    base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    unique_ids_file = os.path.join(base_dir, "../unique_product_ids.txt")
    results_dir = os.path.join(base_dir, "../results")
    
    # 检查文件是否存在
    if not os.path.exists(unique_ids_file):
        print(f"错误: 找不到文件 {unique_ids_file}")
        return
    
    if not os.path.exists(results_dir):
        print(f"错误: 找不到目录 {results_dir}")
        return
    
    # 从第18万行开始读取产品ID
    start_line = 190000
    deleted_count = 0
    checked_count = 0
    
    try:
        with open(unique_ids_file, 'r', encoding='utf-8') as f:
            # 跳过前18万行
            for _ in range(start_line):
                f.readline()
            
            # 读取剩余行并检查对应的HTML文件
            for line in f:
                product_id = line.strip()
                if not product_id:
                    continue
                
                checked_count += 1
                html_file = os.path.join(results_dir, f"{product_id}.html")
                
                if os.path.exists(html_file):
                    try:
                        os.remove(html_file)
                        deleted_count += 1
                        print(f"已删除: {product_id}.html")
                    except Exception as e:
                        print(f"删除文件 {html_file} 时出错: {e}")
                
                # 每处理1000个文件显示一次进度
                if checked_count % 1000 == 0:
                    print(f"已检查 {checked_count} 个文件，已删除 {deleted_count} 个文件")
    
    except Exception as e:
        print(f"处理文件时出错: {e}")
    
    print(f"处理完成! 共检查 {checked_count} 个文件，删除 {deleted_count} 个HTML文件")

if __name__ == "__main__":
    delete_html_files()














