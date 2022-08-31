import pandas as pd
from pathlib import Path
from multiprocessing import Process

import settings


def run_once(dataset):
    all_datasets = []
    print(f'Started {dataset["dataset_name"]}')
    for file_num in range(dataset['csv_count']):
        print(f'Reading {dataset["dataset_name"]} {file_num}')
        num_csv = f"0{file_num}"[-2:]
        df = pd.read_csv(f'{settings.PREPARED_DATASET_FOLDER}/{dataset["dataset_name"]}/{num_csv}.csv', index_col=None, header=0)
        all_datasets.append(df)

    df = pd.concat(all_datasets, axis=0, ignore_index=True)
    for class_id, class_group in df.groupby('class'):
        print(f'Writing {dataset["dataset_name"]} {class_id}')
        Path(f'{settings.BY_CLASS_DATASET_FOLDER}/{dataset["dataset_name"]}/').mkdir(parents=True, exist_ok=True)
        class_group.to_csv(f'{settings.BY_CLASS_DATASET_FOLDER}/{dataset["dataset_name"]}/{class_id}.csv', index=False)
    print(f'Finished {dataset["dataset_name"]}')


def run_all():
    for name, dataset in settings.DATASETS.items():
        run_once(dataset)
    print('All Done')


def run_all_multiprocessing():
    all_processes = []
    for name, dataset in settings.DATASETS.items():
        p = Process(target=run_once, args=(dataset))
        p.start()
        all_processes.append(p)
    for p in all_processes:
        p.join()
    print('All Done')


if __name__ == '__main__':
    run_all()
