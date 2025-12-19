import os

CSV_DIR = '../csvs'
OUTPUT_FILE = '../movie_info.csv'

HEADER = "ASIN,Title,Language,ReleaseDate,Rated,Actors,Directors,Genres,Score,Editions\n"

def merge_files(output_path):
    with open(output_path, 'w', encoding='utf-8', buffering=1024*1024) as outfile:
        outfile.write(HEADER)

        for i, filename in enumerate(os.scandir(CSV_DIR), 1):
            if not filename.is_file():
                continue

            with open(filename.path, 'r', encoding='utf-8') as infile:
                outfile.write(infile.readline())

            if i % 5000 == 0:
                print(f"Merged {i} files...")

if __name__ == '__main__':
    merge_files(OUTPUT_FILE)
    print("Merge Over")
