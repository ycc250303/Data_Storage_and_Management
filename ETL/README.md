查看大txt文件内容

* 打开 `Windows Powershell`
* 用 `get-content`打开文本

ETL步骤

* 运行 `extract_product_ids.py` 从 `movies.txt`获取去重的产品号id `productID`，生成文件 `unique_product_ids.txt`
* 运行 `get_movie_html.py`获取网站的html文件并存储到本地，提取成功的文件会在 `results\`中，超过最大请求测试仍未提取的会记录在 `max_retries.txt`中，需要重新提取
* 运行 `handle_movie_reviews.py`处理 `movies.txt`的评论数据，生成所有评论的时间 `all_review_time.csv`，每个电影最早的评论时间 `earliest_review_time.csv`，每个电影的评论数量 `review_num.csv`
* 运行 `extract_movie_info.py`，提取出每个html文件的电影信息，转换为csv文件记录在 `csv\`中
* 运行 `merge_csvs.py`获取合并后的电影信息合计 `movie_info.csv`
* 运行 `remove_movie_marks.py`获取去除引号的电影信息 `movie_info_marks_cleaned.csv`
* 运行 `merge_name.py`合并相同演员的电影，生成去重后的人名集合 `name_set.pkl`，姓名映射表 `name_map.pkl`，合并名称后的电影信息 `movie_info_name_merged.csv`
* 运行 `merge_movies.py`合并相同电影的不同版本，得到去除标题版本的数据 `movie_info_titled_cleaned.csv`、主ASIN映射关系 `id_mapping.pkl`，电影名+导演对应ASIN关系 `title_directors_mapping.csv`、去重后的数据 `movie_info_movies_merged.csv`
* 运行 `add_release_date.py`得到补充发行时间后的数据 `movie_final.py`
* 运行 `move_files_to_data.py`移动数据位置
