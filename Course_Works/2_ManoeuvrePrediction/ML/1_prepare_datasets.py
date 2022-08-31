import pandas as pd
import numpy as np
from scipy.spatial.distance import cdist
from pathlib import Path
from multiprocessing import Process

import settings


def run_once(dataset, file_num):
    print(f'Starting {dataset["dataset_name"]} {file_num}')
    num_csv = f"0{file_num}"[-2:]
    if Path(f'{settings.PREPARED_DATASET_FOLDER}/{dataset["dataset_name"]}/{num_csv}.csv').is_file():
        print(f'Skipping {dataset["dataset_name"]} {file_num}')
        return
    tracks = pd.read_csv(f"{settings.INITIAL_DATASET_FOLDER}/{dataset['dataset_name']}/data/{num_csv}_tracks.csv")
    tracks = tracks[[
        'recordingId', 'frame', 'trackId',
        'trackLifetime', 'xCenter', 'yCenter', 'heading', 'width', 'length', 'xVelocity', 'yVelocity',
        'xAcceleration', 'yAcceleration', 'lonVelocity', 'latVelocity', 'lonAcceleration', 'latAcceleration'
    ]]
    tracks_meta = pd.read_csv(
        f"{settings.INITIAL_DATASET_FOLDER}/{dataset['dataset_name']}/data/{num_csv}_tracksMeta.csv")
    tracks_meta = tracks_meta[['recordingId', 'trackId', 'class']]
    df = pd.merge(tracks, tracks_meta, on=('recordingId', 'trackId'))

    neighbours_columns = ['distance', 'yVelocity', 'xVelocity', 'yAcceleration', 'xAcceleration', 'xCenter', 'yCenter']
    neighbours_data = []
    for frame_id, frame in df.groupby('frame'):
        coordinates = np.transpose(np.array([frame['xCenter'].values, frame['yCenter'].values]))
        distance_matrix = cdist(coordinates, coordinates, 'euclidean')
        for row_index, distances in enumerate(distance_matrix):
            sorted_indexes = np.argsort(distances)
            neighbours_row = {
                'frame': frame_id,
                'trackId': frame.iloc[row_index]['trackId']
            }
            # excluding self, take 3 closest
            for i in range(1, 4):
                if i < len(distances):
                    index = sorted_indexes[i]
                    for column in neighbours_columns:
                        if column == 'distance':
                            neighbours_row[f'{i}_{column}'] = distances[index]
                        else:
                            neighbours_row[f'{i}_{column}'] = frame.iloc[index][column]
                else:
                    for column in neighbours_columns:
                        neighbours_row[f'{i}_{column}'] = 999999
            neighbours_data.append(neighbours_row)
    neighbours_df = pd.DataFrame(neighbours_data)
    df = pd.merge(df, neighbours_df, on=('frame', 'trackId'))

    Path(f'{settings.PREPARED_DATASET_FOLDER}/{dataset["dataset_name"]}/').mkdir(parents=True, exist_ok=True)
    df.to_csv(f'{settings.PREPARED_DATASET_FOLDER}/{dataset["dataset_name"]}/{num_csv}.csv', index=False)
    print(f'Finished {dataset["dataset_name"]} {file_num}')


def run_all():
    for name, dataset in settings.DATASETS.items():
        for file_num in range(dataset['csv_count']):
            run_once(dataset, file_num)
    print('All Done')


def run_all_multiprocessing():
    all_processes = []
    for name, dataset in settings.DATASETS.items():
        for file_num in range(dataset['csv_count']):
            p = Process(target=run_once, args=(dataset, file_num))
            p.start()
            all_processes.append(p)
    for p in all_processes:
        p.join()
    print('All Done')


if __name__ == '__main__':
    run_all()
