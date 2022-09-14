#include <iostream>
#include <fstream>
#include <vector>

///Из файла get_path получает наблюдения, первые значения которых сохраняет
///в массив a (оценка воды A), вторые - в массив b (оценка воды B). Если внутри 
///одного наблюдения оценка воды A совпадает с оценкой воды B, то наблюдение игнорируется.
///Выборка без наблюдений с равными оценками созраняется в файл save_path.
void get_data(const std::string &get_path, const std::string &save_path,
              std::vector<int> &a, std::vector<int> &b) {
    //Если массивы были непусты, то очищаем их.
    a.erase(a.begin(), a.end());
    b.erase(b.begin(), b.end());

    std::ifstream fin;
    fin.open(get_path);
    std::ofstream fout;
    fout.open(save_path);

    //Переменные для считывания наблюдений.
    int cur_sex, cur_a, cur_b;
    while (fin >> cur_sex >> cur_a >> cur_b) {
        //Если оценки не равны, то добавляем данное наблюдение
        //(иначе - игнорируем).
        if (cur_a != cur_b) {
            a.push_back(cur_a);
            b.push_back(cur_b);
            fout << cur_sex << " " << cur_a << " " << cur_b << "\n";
        }
    }

    fin.close();
    fout.close();
}

///Вычисляет и сохраняет в файл path: количество наблюдений, в которых оценка воды A больше
///оценки воды B (n_plus); количество наблюдений, в которых оценка воды B больше
///оценки воды A (n_minus); суммарное количество наблюдений (n_plus + n_minus).
///Если встречаются наблюдений с одинаковыми оценками, выбрасываеся std::logic_error.
///Возвращает массив, первый элемент которого - n_plus, второй - n_minus.
int *calculate_n_plus_and_n_minus(const std::string &path, const std::vector<int> &a,
                                  const std::vector<int> &b) {
    std::ofstream fout;
    fout.open(path);

    int n_plus = 0, n_minus = 0;
    for (int i = 0; i < a.size(); i++) {
        if (a[i] > b[i]) {
            n_plus++;
        } else if (a[i] < b[i]) {
            n_minus++;
        } else {
            throw std::logic_error("There should be no equal appraisals. ");
        }
    }
    fout << "n^+ = " << n_plus << "\nn^- = " << n_minus << "\n" <<
         "n = n^+ + n^- = " << n_plus + n_minus;

    fout.close();
    int *n = new int[2];
    n[0] = n_plus;
    n[1] = n_minus;
    return n;
}

///Вычисляет и возвращает число сочетаний из n по k.
long long calculate_number_of_combinations(int k, int n) {
    if (k == 0) {
        return n;
    }
    long long res = n - k + 1;
    for (int i = n - k + 2; i <= n; i++) {
        res *= i;
        res /= (i - n + k);
    }
    return res;
}

///Вычисляет, сохраняет в файл path и возвращает критическое значение для n наблюдение
///на уровне значимости alpha.
int calculate_critical_value(const std::string &path, const int n, const double alpha) {
    std::ofstream fout;
    fout.open(path);

    //Так как во всех слагаемых присутствует постоянный коэффициент 0.5^n,
    //его можно вынести. Поделив обе части неравенства на этот коэффициент,
    //в правой части получаем alpha / 0.5^n = 2^n * alpha.
    long long helper = 1;
    for (int i = 0; i < n; i++) {
        helper *= 2;
    }
    //В таком случае, в левой части останутся только целые числа. Тогда, не имеет
    //смысла хранить нецелое число - округлив в меньшую сторону, мы не повлияем
    //на ответ.
    helper = static_cast<long long>(static_cast<double>(helper) * alpha);

    int k;
    long long p = 0;
    for (k = -1; p + calculate_number_of_combinations(k + 1, n) <= helper; k++) {
        p += calculate_number_of_combinations(k + 1, n);
    }
    fout << "T_{n; alpha} ";
    if (k == -1) {
        fout << " does not exist";
    } else {
        fout << "= " << k;
    }

    fout.close();
    return k;
}

int main() {
    //Массивы для хранения наблюдение. a - оценка воды A; b - оценка воды B. Пол в данном
    //вопросе не фигурирует, поэтому мы не будем сохранять его программно.
    std::vector<int> a, b;

    get_data("data.txt", "data_without_equal_appraisals.txt", a, b);
    int *n = calculate_n_plus_and_n_minus("n_plus_and_n_minus.txt", a, b);
    calculate_critical_value("critical_value.txt", n[0] + n[1], 0.1);

    delete[] n;
    return 0;
}
