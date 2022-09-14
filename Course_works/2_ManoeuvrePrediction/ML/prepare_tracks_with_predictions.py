import pandas as pd
from pathlib import Path
from multiprocessing import Process

import settings


def get_prediction(row):
    FIRST_DEGREE_TURN = 5
    SECOND_DEGREE_TURN = 10
    heading_change = (row['end_heading']-row['heading'] + 180) % 360 - 180

    if row['lonVelocity'] < 0.3 and row['end_lonVelocity'] < 0.3:
        return 'still'
    elif heading_change >= SECOND_DEGREE_TURN:
        return 'turn-left'
    elif -heading_change >= SECOND_DEGREE_TURN:
        return 'turn-right'
    elif FIRST_DEGREE_TURN <= heading_change < SECOND_DEGREE_TURN:
        return 'easy-turn-left'
    elif FIRST_DEGREE_TURN <= -heading_change < SECOND_DEGREE_TURN:
        return 'easy-turn-right'
    elif (2*row['end_lonVelocity']/(row['lonVelocity']+row['end_lonVelocity'])) > 1.1:
        return 'faster'
    elif (2*row['end_lonVelocity']/(row['lonVelocity']+row['end_lonVelocity'])) < 0.9:
        return 'slower'
    else:
        return 'constant-speed'


def run_once(dataset, file_num):
    print(f'Starting {dataset["dataset_name"]} {file_num}')
    num_csv = f"0{file_num}"[-2:]
    if Path(f"{settings.TRACKS_PREDICTION_FOLDER}/{dataset['dataset_name']}/predictions/{num_csv}_tracks_predictions.csv").is_file():
        print(f'Skipping {dataset["dataset_name"]} {file_num}')
        return
    df = pd.read_csv(f"{settings.INITIAL_DATASET_FOLDER}/{dataset['dataset_name']}/data/{num_csv}_tracks.csv")
    all_datasets = []
    for (track_id, recording_id), group in df.groupby(['trackId', 'recordingId']):
        temp_columns = ['heading', 'xCenter', 'yCenter', 'lonVelocity']
        for column in temp_columns:
            group[f'end_{column}'] = group[column].shift(-settings.PREDICTION_FORWARD_FRAMES,
                                                         fill_value=group.iloc[-1][column])
        group['prediction'] = group.apply(get_prediction, axis=1)
        group = group.drop(columns=[f'end_{item}' for item in temp_columns])
        all_datasets.append(group[['frame', 'trackId', 'prediction']])
    prediction_df = pd.concat(all_datasets, axis=0, ignore_index=True)
    Path(f"{settings.TRACKS_PREDICTION_FOLDER}/{dataset['dataset_name']}/predictions/").mkdir(parents=True, exist_ok=True)
    prediction_df.to_csv(f"{settings.TRACKS_PREDICTION_FOLDER}/{dataset['dataset_name']}/predictions/{num_csv}_tracks_predictions.csv", index=False)
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
    run_all_multiprocessing()
