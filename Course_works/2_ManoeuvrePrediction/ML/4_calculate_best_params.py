from pathlib import Path
import pandas as pd
from sklearn.model_selection import GridSearchCV
from sklearn.tree import DecisionTreeClassifier
import json

import settings

SEARCH_PARAMS = {
    'criterion': ['gini', 'entropy'],
    'max_depth': range(1, 10),
    'min_samples_split': range(2, 10),
    'min_samples_leaf': range(1, 5)
}


def run_once(dataset, filepath):
    class_name = filepath.name[:-4]
    print(f'Starting {dataset["dataset_name"]} {class_name}')
    if Path(f'{settings.BEST_PARAMETERS_FOLDER}/{dataset["dataset_name"]}/{class_name}.json').is_file():
        print(f'Skipping {dataset["dataset_name"]} {class_name}')
        return
    df = pd.read_csv(filepath)
    df = df.drop(columns=['recordingId', 'frame', 'trackId', 'class', 'trackLifetime'])
    print(df['prediction'].value_counts())
    x = df.drop('prediction', axis=1)
    y = df['prediction']
    y, prediction_names = pd.factorize(y)
    decision_tree = DecisionTreeClassifier()
    clf = GridSearchCV(decision_tree, param_grid=SEARCH_PARAMS, cv=4, verbose=1, n_jobs=-1)
    clf.fit(x, y)
    with open(f'{settings.BEST_PARAMETERS_FOLDER}/{dataset["dataset_name"]}/{class_name}.json', 'w') as f:
        json.dump(clf.best_params_, f)
    print(f'Finished {dataset["dataset_name"]} {class_name}')


def run_all():
    for name, dataset in settings.DATASETS.items():
        Path(f'{settings.BEST_PARAMETERS_FOLDER}/{dataset["dataset_name"]}/').mkdir(parents=True, exist_ok=True)
        for filepath in Path(f'{settings.LABELED_DATASET_FOLDER}/{dataset["dataset_name"]}/').glob('*.csv'):
            run_once(dataset, filepath)


if __name__ == '__main__':
    run_all()
