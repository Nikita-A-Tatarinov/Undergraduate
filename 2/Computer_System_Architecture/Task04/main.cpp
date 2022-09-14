#include "omp.h"
#include <iostream>
#include <fstream>
#include <thread>
#include <stack>
#include <condition_variable>
#include <random>

///Блокировщик потоков в моменты добавления в стек (удаления из стека) и
///вывода комментариев в консоль.
omp_lock_t lock;
///Отражает, считал ли поток-производитель все данные из входного файла.
bool file_is_empty = false;

///Представляет собой класс для обмена данными между
///производителями и потребителями.
template<class T>
class Buffer {
private:
    ///Максимальное количество элементов в буфере.
    static const size_t st_max_size = 5;
    ///Контейнер для хранения элементов буфера.
    std::stack<T>* st;

public:
    Buffer();
    ~Buffer();

    ///Добавляет новый элемент в буфер.
    void add(const T& newElement);

    ///Удалаяет верхний элемент из буфера.
    T remove();
};

///Представляет собой класс производителя.
class Producer{
private:
    ///Буфер наименований товаров.
    Buffer<std::string>* units;
    ///Буфер цен товаров.
    Buffer<unsigned int>* prices;
    ///Файл, откуда считываются товары.
    std::ifstream fin;

public:
    Producer(Buffer<std::string>* _units,
             Buffer<unsigned int>* _prices,
             const std::string& path);
    ~Producer();

    ///Метод работы с данными.
    void run();
};

///Представляет собой класс потребителя.
template<class T>
class Consumer{
private:
    ///Буфер товаров (наименований или стоимостей).
    Buffer<T>* objects;
    ///Файл, в который выводятся товары.
    std::ofstream fout;
    ///Суммарная стоимость товаров (если потребитель
    ///хранит стоимости).
    unsigned long long overall_price;

public:
    Consumer(Buffer<T>* _objects, const std::string& path);
    ~Consumer();

    ///Метод работы с товарами.
    void run();
};

///Метод генерации случайной строки из латинских букв и цифр
///длиной от 10 до 15 символов.
static std::string generate_random_string(std::mt19937& engine);

///Метод создания файла с товарами.
static void create_input_file();

int main() {
    create_input_file();
    auto units = new Buffer<std::string>();
    auto prices = new Buffer<unsigned int>();
    auto Ivanov = new Producer(units, prices, "warehouse.txt");
    auto Petrov = new Consumer<std::string>(units, "truck.txt");
    auto Necheporchuk = new Consumer<unsigned int>(prices, "price_list.txt");

#pragma omp parallel num_threads(4)
    {
        omp_init_lock(&lock);
#pragma omp sections
        {
#pragma omp section
            {
                Ivanov->run();
            }
#pragma omp section
            {
                Petrov->run();
            }
#pragma omp section
            {
                Necheporchuk->run();
            }
        }
        omp_destroy_lock(&lock);
    }

    delete Ivanov;
    delete Petrov;
    delete Necheporchuk;
    delete units;
    delete prices;
    return 0;
}

static std::string generate_random_string(std::mt19937& engine){
    std::uniform_int_distribution<unsigned char> dist1(10, 15);
    std::uniform_int_distribution<unsigned char> dist2(0, 61);
    std::string chars_to_use = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    std::string result;
    unsigned char result_length = dist1(engine);
    for(size_t i = 0; i < result_length; i++){
        result += chars_to_use[dist2(engine)];
    }
    return result;
}

static void create_input_file(){
    std::ofstream fout;
    fout.open("warehouse.txt");
    std::random_device rd;
    std::mt19937 engine(rd());
    //Количество товаров на складе.
    std::uniform_int_distribution<unsigned char> dist1(10, 50);
    //Цена каждого товара.
    std::uniform_int_distribution<unsigned int> dist2(1000, 100000);
    unsigned char amount_of_units = dist1(engine);
    for(unsigned char i = 0; i < amount_of_units; i++){
        fout << generate_random_string(engine) << " " << dist2(engine) << "\n";
    }
    fout.close();
}

template<class T>
Buffer<T>::Buffer() {
    st = new std::stack<T>();
}

template<class T>
Buffer<T>::~Buffer(){
    delete st;
}

template<class T>
void Buffer<T>::add(const T& newElement) {
    if(st->size() == st_max_size){
        omp_unset_lock(&lock);
        while(st->size() == st_max_size);
        omp_set_lock(&lock);
    }
    st->push(newElement);
}

template<class T>
T Buffer<T>::remove(){
    if(st->empty()){
        omp_unset_lock(&lock);
        while(st->empty()){
            if(file_is_empty){
                break;
            }
        }
        omp_set_lock(&lock);
    }
    T top = st->top();
    st->pop();
    return top;
}

Producer::Producer(Buffer<std::string>* _units,
                   Buffer<unsigned int>* _prices,
                   const std::string& path) : units(_units), prices(_prices){
    fin.open(path);
}

Producer::~Producer() {
    units = nullptr;
    prices = nullptr;
    fin.close();
}

void Producer::run() {
    std::string unit;
    unsigned int price;
    while(fin >> unit >> price){
        omp_set_lock(&lock);
        units->add(unit);
        prices->add(price);
        std::cout << "Unit \"" << unit << "\" of price " << price <<
                  " is taken out of the warehouse\n";
        omp_unset_lock(&lock);
    }
    file_is_empty = true;
}

template<class T>
Consumer<T>::Consumer(Buffer<T>* _objects, const std::string& path)
            : objects(_objects), overall_price(0){
    fout.open(path);
}

template<class T>
Consumer<T>::~Consumer() {
    if(typeid(T).name() == typeid(unsigned int).name()) {
        omp_set_lock(&lock);
        std::cout << "Overall price is " << overall_price << "\n";
        fout << "Overall price is " << overall_price << "\n";
        omp_unset_lock(&lock);
    }
    objects = nullptr;
    fout.close();
}

template<class T>
void Consumer<T>::run() {
    while(!file_is_empty) {
        omp_set_lock(&lock);
        T unit = objects->remove();
        if (typeid(T) == typeid(std::string)) {
            fout << unit << "\n";
            std::cout << "Unit \"" << unit << "\" is entrained in the truck\n";
        } else if (typeid(T) == typeid(unsigned int)) {
            overall_price += *reinterpret_cast<unsigned int *>(&unit);
            fout << "+" << unit << "\n";
            std::cout << "+" << unit << " in price list\n";
        }
        omp_unset_lock(&lock);
    }
}
