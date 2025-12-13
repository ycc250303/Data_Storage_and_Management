import os

CSV_DIR = '../csvs'

HEADER = "ASIN,Title,Language,ReleaseDate,Rated,Actors,Directors,Genres,Score,Editions\n"

def merge_files(output_path):
    with open(output_path, 'w', encoding='utf-8') as outfile:
        # 写入固定标题
        outfile.write(HEADER)
        for filename in os.listdir(CSV_DIR):
            with open(os.path.join(CSV_DIR, filename), 'r', encoding='utf-8') as infile:
                outfile.write(infile.readline())

if __name__ == '__main__':
    merge_files('../movie_info.csv')
    print("Merge Over")
