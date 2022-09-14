#include <iostream>
#include <fstream>
#include <algorithm>
#include <vector>

///Компаратор для сортировки по возрастанию оценки воды A.
bool cmp(std::pair<std::pair<int, int>, int> i,
         std::pair<std::pair<int, int>, int> j) {
    return i.first.second < j.first.second;
}

///Из файла get_path получает наблюдения. Значения оценки для воды B игнорируются.
///Значения пола и оценок для воды A сохраняются в массив sex_a_rank в отсортированном
///по оецнке воды A порядке (нулевой элемент - пол, первый элемент - оценка для воды A,
///второй элемент - ранг наблюдения). Полученные данные сохраняет в файл save_path.
void get_data(const std::string &get_path, const std::string &save_path,
              std::vector<std::pair<std::pair<int, int>, double>> &sex_a_rank) {
    //Если массив были непуст, то очищаем его.
    sex_a_rank.erase(sex_a_rank.begin(), sex_a_rank.end());

    std::ifstream fin;
    fin.open(get_path);
    std::ofstream fout;
    fout.open(save_path);

    //Переменные для считывания наблюдений.
    int cur_sex, cur_a, cur_b;
    while (fin >> cur_sex >> cur_a >> cur_b) {
        sex_a_rank.emplace_back(std::pair<int, int>(cur_sex, cur_a), 0);
    }
    //Сортировка по возрастанию оценки воды A.
    sort(sex_a_rank.begin(), sex_a_rank.end(), cmp);
    //Заполняем ранги.
    for (int i = 0, j, in_a_row = 1; i < sex_a_rank.size(); i++) {
        if (i == sex_a_rank.size() - 1 ||
            sex_a_rank[i].first.second != sex_a_rank[i + 1].first.second) {
            double helper = i + 1 - (in_a_row - 1) / 2.0;
            for (j = i - in_a_row + 1; j < i + 1; j++) {
                sex_a_rank[j].second = helper;
            }
            in_a_row = 1;
        } else {
            in_a_row++;
        }
    }
    for (auto &i : sex_a_rank) {
        fout << i.first.first << " " << i.first.second << " " << i.second << "\n";
    }

    fin.close();
    fout.close();
}

///Вычисляет и сохраняет в файл path: количество наблюдений, в которых пол
///человека - мужской (n0); количество наблюдений, в которых пол человека -
///женский (n1); суммарное количество наблюдений (n0 + n1).
///Возвращает массив, первый элемент которого - n0, второй - n1.
int *calculate_n0_and_n1(const std::string &path,
                         const std::vector<std::pair<std::pair<int, int>, double>> &sex_a_rank) {
    std::ofstream fout;
    fout.open(path);

    int n0 = 0, n1 = 0;
    for (auto &i : sex_a_rank) {
        if (i.first.first) {
            n1++;
        } else {
            n0++;
        }
    }
    fout << "n0 = " << n0 << "\nn1 = " << n1 << "\nn = n0 + n1 = " << n0 + n1;

    int *n = new int[2];
    n[0] = n0;
    n[1] = n1;
    return n;
}

///Вычисляет, сохраняет в файл path и возвращает значение W-статистики для набора
///наблюдений sex_a_rank.
double calculate_Wilcoxon_statistics(const std::string &path,
                                     const std::vector<
                                             std::pair<std::pair<int, int>, double>> &sex_a_rank) {
    std::ofstream fout;
    fout.open(path);

    double res = 0;
    //Во избежание проверки пола можно домножить на (1 - пол): так как женский пол
    //закодирован нулём, а мужской единицей, то при домножении каждого ранга на
    //(1 - пол) получаем сумму рангов наблюдений, где пол человека - мужской, то есть
    //получаем искомую статистику.
    for (auto &i : sex_a_rank) {
        res += (1 - i.first.first) * i.second;
    }
    fout << "W = " << res;

    fout.close();
    return res;
}

int main() {
    std::vector<std::pair<std::pair<int, int>, double>> sex_a_rank;

    get_data("data.txt", "ranked_data.txt", sex_a_rank);
    delete[] calculate_n0_and_n1("n0_and_n1.txt", sex_a_rank);
    calculate_Wilcoxon_statistics("Wilcoxon_statistics.txt", sex_a_rank);

    return 0;
}
