#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
测试extract_movie_info.py中的parsePrimePage函数
输入：本地HTML文件
输出：打印提取的电影信息
"""

import sys
import os
from lxml import etree

# 添加当前目录到Python路径，以便导入模块
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from extract_movie_info import Parser, MovieInfo, remove_brackets_content

def test_parse_prime_page(html_file_path):
    """
    测试parsePrimePage函数
    :param html_file_path: HTML文件路径
    """
    if not os.path.exists(html_file_path):
        print(f"错误：文件 {html_file_path} 不存在")
        return
    
    # 读取HTML文件
    with open(html_file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # 解析HTML
    page = etree.HTML(content)
    
    # 获取文件名（不含扩展名）作为ASIN
    file_name = os.path.basename(html_file_path)
    asin = os.path.splitext(file_name)[0]
    
    # 创建MovieInfo对象
    info = MovieInfo(asin)
    
    # 手动调用parsePrimePage函数的逻辑，但不写入文件
    # 获取电影标题 - Prime Video页面使用不同的选择器
    title = page.xpath('normalize-space(//h1[@data-automation-id="title"]/text())')
    if not title:
        title = page.xpath('normalize-space(//title/text())')
        if title and '|' in title:
            title = title.split('|')[0].strip()
    info.title = title

    # 获取电影语言 - Prime Video页面的语言信息
    audio_languages = page.xpath('normalize-space(//h3/span[text()="Audio languages"]/../../dd/text())')
    if not audio_languages:
        # 尝试其他可能的选择器
        audio_languages = page.xpath('normalize-space(//span[text()="Audio languages"]/../../../dd/text())')
    if audio_languages:
        info.language = remove_brackets_content(audio_languages)

    # 获取上映日期 - Prime Video页面的发布年份
    release_year = page.xpath('normalize-space(//span[@data-automation-id="release-year-badge"]/text())')
    if release_year:
        info.release_date = release_year

    # 获取电影评级 - Prime Video页面的内容评级
    rated = page.xpath('normalize-space(//span[@data-automation-id="rating-badge"]/text())')
    if rated:
        info.rated = rated

    # 获取电影演员 - Prime Video页面的演员信息
    actors_elements = page.xpath('//h3/span[text()="Cast"]/../../dd/a/text()')
    if not actors_elements:
        # 尝试其他可能的选择器
        actors_elements = page.xpath('//span[text()="Cast"]/../../../dd/a/text()')
    if actors_elements:
        info.actors = ', '.join(actors_elements)

    # 获取电影导演 - Prime Video页面的导演信息
    directors = page.xpath('//h3/span[text()="Directors"]/../../dd/a/text()')
    if not directors:
        # 尝试其他可能的选择器
        directors = page.xpath('//span[text()="Directors"]/../../../dd/a/text()')
    if directors:
        info.director = ', '.join(directors)

    # 获取电影评分 - Prime Video页面的星级评分
    div_elements = page.xpath('//div[@aria-label]')
    for div in div_elements:
        aria_label = div.get('aria-label', '')
        if 'Rated' in aria_label and 'out of 5 stars' in aria_label:
            # 提取评分数字，例如从"Rated 2.8 out of 5 stars by 11 Amazon customers."中提取"2.8"
            import re
            match = re.search(r'Rated (\d+\.\d+) out of 5 stars', aria_label)
            if match:
                info.score = match.group(1)
            break
    

    # 获取电影其他版本 - Prime Video页面的其他格式
    # 尝试多种选择器以确保获取所有版本
    other_formats = page.xpath('//div[@data-automation-id="other-formats"]//strong/text()')
    if not other_formats:
        other_formats = page.xpath('//h3[text()="Other formats"]/following-sibling::div//strong/text()')
    if not other_formats:
        other_formats = page.xpath('//h3[contains(text(), "Other formats")]/following-sibling::div//a//strong/text()')
    
    for format_name in other_formats:
        if format_name and format_name.strip():
            info.editions.append(format_name.strip())

    # 获取同一部电影的不同id - Prime Video页面的相关ID
    # 只从"Other formats"部分提取相关ID，确保准确性
    other_format_links = page.xpath('//div[@data-automation-id="other-formats"]//a[contains(@href, "/dp/")]/@href')
    if not other_format_links:
        other_format_links = page.xpath('//h3[text()="Other formats"]/following-sibling::div//a[contains(@href, "/dp/")]/@href')
    
    for href in other_format_links:
        if '/dp/' in href:
            try:
                related_id = href.split('/dp/')[1][:10]
                # 确保ID是10位字母数字组合
                if len(related_id) == 10 and related_id.isalnum():
                    info.related_ids.add(related_id)
            except:
                pass
    
    # 打印提取的信息
    print("=" * 50)
    print(f"电影信息 (ASIN: {asin})")
    print("=" * 50)
    print(f"电影名称: {info.title}")
    print(f"电影语言: {info.language}")
    print(f"上映日期: {info.release_date}")
    print(f"电影评级: {info.rated}")
    print(f"电影演员: {info.actors}")
    print(f"电影导演: {info.director}")
    print(f"电影评分: {info.score}")
    print(f"电影其他版本: {', '.join(info.editions)}")
    print(f"相关ID: {', '.join(info.related_ids)}")
    print("=" * 50)
    
    # 检查是否是电影
    if info.isMovie():
        print("✓ 确认为电影")
    else:
        print("✗ 可能不是电影（信息不完整）")

def main():
    # 直接在代码中指定HTML文件路径
    html_file_path = "../results/B004VZW92W.html"
    
    if not os.path.exists(html_file_path):
        print(f"错误：文件 {html_file_path} 不存在")
        return
    
    test_parse_prime_page(html_file_path)

if __name__ == "__main__":
    main()
