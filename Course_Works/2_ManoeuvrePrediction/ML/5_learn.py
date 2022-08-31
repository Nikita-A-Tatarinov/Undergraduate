from pathlib import Path
import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.tree import DecisionTreeClassifier
from sklearn import metrics
from sklearn.metrics import confusion_matrix, classification_report
import json
from sklearn2pmml.pipeline import PMMLPipeline
from sklearn2pmml import sklearn2pmml

import graphviz
from sklearn import tree

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
    Path(f'{settings.LEARNED_DATA_FOLDER}/{dataset["dataset_name"]}/{class_name}/').mkdir(parents=True, exist_ok=True)
    df = pd.read_csv(filepath)
    df = df.drop(columns=['recordingId', 'frame', 'trackId', 'class'])
    print(df['prediction'].value_counts())
    with open(f'{settings.LEARNED_DATA_FOLDER}/{dataset["dataset_name"]}/{class_name}/value_counts.txt', 'w') as f:
        f.write(str(df['prediction'].value_counts()))
    x = df.drop('prediction', axis=1)
    y = df['prediction']
    y, prediction_names = pd.factorize(y, sort=True)
    with open(f'{settings.LEARNED_DATA_FOLDER}/{dataset["dataset_name"]}/{class_name}/prediction_names.json', 'w') as f:
        json.dump({i: item for i, item in enumerate(prediction_names)}, f)
    x_train, x_test, y_train, y_test = train_test_split(x, y, test_size=0.25, stratify=y, random_state=42)
    with open(f'{settings.BEST_PARAMETERS_FOLDER}/{dataset["dataset_name"]}/{class_name}.json', 'r') as f:
        params = json.load(f)
    classifier = DecisionTreeClassifier(**params, random_state=42)
    classifier.fit(x_train, y_train)

    y_train_pred = classifier.predict(x_train)
    accuracy = metrics.accuracy_score(y_train, y_train_pred)
    print("Accuracy: {:.2f}".format(accuracy))
    cm = confusion_matrix(y_train, y_train_pred)
    print('Confusion Matrix: \n', cm)
    with open(f'{settings.LEARNED_DATA_FOLDER}/{dataset["dataset_name"]}/{class_name}/train_cm.txt', 'w') as f:
        f.write(str(cm))
    cr = classification_report(y_train, y_train_pred, target_names=prediction_names)
    print('Classification Report: \n', cr)
    with open(f'{settings.LEARNED_DATA_FOLDER}/{dataset["dataset_name"]}/{class_name}/train_cr.txt', 'w') as f:
        f.write(str(cr))

    y_test_pred = classifier.predict(x_test)
    accuracy = metrics.accuracy_score(y_test, y_test_pred)
    print("Accuracy: {:.2f}".format(accuracy))
    cm = confusion_matrix(y_test, y_test_pred)
    print('Confusion Matrix: \n', cm)
    with open(f'{settings.LEARNED_DATA_FOLDER}/{dataset["dataset_name"]}/{class_name}/test_cm.txt', 'w') as f:
        f.write(str(cm))
    cr = classification_report(y_test, y_test_pred, target_names=prediction_names)
    print('Classification Report: \n', cr)
    with open(f'{settings.LEARNED_DATA_FOLDER}/{dataset["dataset_name"]}/{class_name}/test_cr.txt', 'w') as f:
        f.write(str(cr))

    tree.export_graphviz(classifier, out_file=f'{settings.LEARNED_DATA_FOLDER}/{dataset["dataset_name"]}/{class_name}/graphviz.dot', filled=True, rounded=True, feature_names=x.columns, class_names=prediction_names)
    graph = graphviz.Source.from_file(f'{settings.LEARNED_DATA_FOLDER}/{dataset["dataset_name"]}/{class_name}/graphviz.dot')
    graph.render(f'{settings.LEARNED_DATA_FOLDER}/{dataset["dataset_name"]}/{class_name}/graphviz', format='pdf')

    pipeline = PMMLPipeline([("classifier", classifier)])
    Path(f'{settings.TREES_FOLDER}/{dataset["dataset_name"]}/').mkdir(parents=True, exist_ok=True)
    sklearn2pmml(pipeline, f'{settings.TREES_FOLDER}/{dataset["dataset_name"]}/{class_name}.pmml', with_repr=True)
    print(f'Finished {dataset["dataset_name"]} {class_name}')


def run_all():
    for name, dataset in settings.DATASETS.items():
        for filepath in Path(f'{settings.LABELED_DATASET_FOLDER}/{dataset["dataset_name"]}/').glob('*.csv'):
            run_once(dataset, filepath)


if __name__ == '__main__':
    run_all()