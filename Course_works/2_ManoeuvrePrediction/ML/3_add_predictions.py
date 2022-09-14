import pandas as pd
from pathlib import Path
from multiprocessing import Process
import os

import settings
from prepare_tracks_with_predictions import get_prediction


def run_once(dataset, filepath):
    class_name = filepath.name[:-4]
    print(f'Starting {dataset["dataset_name"]} {class_name}')
    if Path(f'{settings.LABELED_DATASET_FOLDER}/{dataset["dataset_name"]}/{class_name}.csv').is_file():
        print(f'Skipping {dataset["dataset_name"]} {class_name}')
        return
    df = pd.read_csv(filepath)
    all_datasets = []
    for (track_id, recording_id), group in df.groupby(['trackId', 'recordingId']):
        temp_columns = ['heading', 'xCenter', 'yCenter', 'lonVelocity']
        group = group.sort_values(by=['frame'])
        for column in temp_columns:
            group[f'end_{column}'] = group[column].shift(-settings.PREDICTION_FORWARD_FRAMES,
                                                         fill_value=group.iloc[-1][column])
        group['prediction'] = group.apply(get_prediction, axis=1)
        group = group.drop(columns=[f'end_{item}' for item in temp_columns])
        all_datasets.append(group)
    df = pd.concat(all_datasets, axis=0, ignore_index=True)
    Path(f'{settings.LABELED_DATASET_FOLDER}/{dataset["dataset_name"]}/').mkdir(parents=True, exist_ok=True)
    df.to_csv(f'{settings.LABELED_DATASET_FOLDER}/{dataset["dataset_name"]}/{class_name}.csv', index=False)
    print(f'Finished {dataset["dataset_name"]} {class_name}')


def exclude_ind_car_stationary():
    still_limit_percentage = 0.9
    dataset_name = 'inD-dataset-v1.0'
    class_name = 'car'
    if Path(f'{settings.LABELED_DATASET_FOLDER}/{dataset_name}/unfiltered/{class_name}.csv').is_file():
        print(f'Skipping, already filtered')
        return
    Path(f'{settings.LABELED_DATASET_FOLDER}/{dataset_name}/unfiltered/').mkdir(parents=True, exist_ok=True)
    os.rename(
        rf'{settings.LABELED_DATASET_FOLDER}/{dataset_name}/{class_name}.csv',
        rf'{settings.LABELED_DATASET_FOLDER}/{dataset_name}/unfiltered/{class_name}.csv'
    )
    df = pd.read_csv(f'{settings.LABELED_DATASET_FOLDER}/{dataset_name}/unfiltered/{class_name}.csv')
    print('before:\n', df['prediction'].value_counts())
    all_datasets = []
    for (track_id, recording_id), group in df.groupby(['trackId', 'recordingId']):
        value_count = group['prediction'].value_counts(normalize=True)
        still_pecentage = value_count.get('still', 0)
        if still_pecentage > still_limit_percentage:
            print(group['prediction'].value_counts())
        else:
            all_datasets.append(group)
    df = pd.concat(all_datasets, axis=0, ignore_index=True)
    df.to_csv(f'{settings.LABELED_DATASET_FOLDER}/{dataset_name}/{class_name}.csv', index=False)
    print('after:', df['prediction'].value_counts())


def run_all():
    for name, dataset in settings.DATASETS.items():
        for filepath in Path(rf'{settings.BY_CLASS_DATASET_FOLDER}/{dataset["dataset_name"]}/').glob('*.csv'):
            run_once(dataset, filepath)
    print('All Done')


def run_all_multiprocessing():
    all_processes = []
    for name, dataset in settings.DATASETS.items():
        for filepath in Path(rf'{settings.BY_CLASS_DATASET_FOLDER}/{dataset["dataset_name"]}/').glob('*.csv'):
            p = Process(target=run_once, args=(dataset, filepath))
            p.start()
            all_processes.append(p)
    for p in all_processes:
        p.join()
    print('All Done')


if __name__ == '__main__':
    run_all_multiprocessing()
    exclude_ind_car_stationary()
