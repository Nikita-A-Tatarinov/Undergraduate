#include <iostream>
#include <fstream>
#include <thread>
#include <stack>
#include <mutex>
#include <condition_variable>
#include <random>

std::mutex buf_mu, pr_con_mu;
std::condition_variable cond;
bool file_is_empty = false;

///Представляет собой класс для обмена данными между
///производителями и потребителями.
template<class T>
class Buffer {
private:
    ///Максимальное количество элементов в буфере.
    static const size_t st_max_size = 5;
    ///Контейнер для хранения элементов буфера.
    std::stack<T> *st;

public:
    Buffer();
    ~Buffer();

    ///Добавляет новый элемент в буфер.
    void add(const T& newElement);

    ///Удалаяет верхний элемент из буфера.
    T remove();

    ///Возвращает true, если буфер пуст.
    [[nodiscard]] bool empty() const;
};

///Представляет собой класс производителя.
class Producer{
private:
    ///Буфер наименований товаров.
    Buffer<std::string> *units;
    ///Буфер цен товаров.
    Buffer<unsigned int> *prices;
    ///Файл, откуда считываются товары.
    std::ifstream fin;
    ///Предоставляем классу Consumer доступ к
    ///приватным полям и методам.
    template <class>
    friend class Consumer;

public:
    Producer(Buffer<std::string> *_units,
              Buffer<unsigned int> *_prices,
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
    Buffer<T> *objects;
    ///Файл, в который выводятся товары.
    std::ofstream fout;
    ///Суммарная стоимость товаров (если потребитель
    ///хранит стоимости).
    unsigned long long overall_price;

public:
    explicit Consumer(const std::string& path);
    ~Consumer();

    ///Метод работы с товарами.
    void run();

    ///Получает от потребителя буфер для работы.
    void get_buffer(const Producer *pr);
};

///Метод генерации случайной строки из латинских букв и цифр
///длиной от 10 до 15 символов.
static std::string generate_random_string();

///Метод создания файла с товарами.
static void create_input_file();

int main() {
    create_input_file();
    auto *units = new Buffer<std::string>();
    auto *prices = new Buffer<unsigned int>();
    auto Ivanov = new Producer(units, prices, "warehouse.txt");
    auto Petrov = new Consumer<std::string>("truck.txt");
    auto Necheporchuk = new Consumer<unsigned int>("price_list.txt");
    Petrov->get_buffer(Ivanov);
    Necheporchuk->get_buffer(Ivanov);

    std::thread Ivanov_thread(&Producer::run, Ivanov);
    std::thread Petrov_thread(&Consumer<std::string>::run, Petrov);
    std::thread Necheporchuk_thread(&Consumer<unsigned int>::run, Necheporchuk);

    Ivanov_thread.join();
    file_is_empty = true;
    Petrov_thread.join();
    Necheporchuk_thread.join();
    delete Necheporchuk;
    delete Petrov;
    delete Ivanov;
    delete prices;
    delete units;
    return 0;
}

static std::string generate_random_string(){
    thread_local std::random_device rd;
    thread_local std::mt19937 engine(rd());
    thread_local std::uniform_int_distribution<unsigned char> dist1(10, 15);
    thread_local std::uniform_int_distribution<unsigned char> dist2(0, 61);
    std::string chars_to_use = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    std::string result;
    unsigned char str_length = dist1(engine);
    for(size_t i = 0; i < str_length; i++){
        result += chars_to_use[dist2(engine)];
    }
    return result;
}

static void create_input_file(){
    std::ofstream fout;
    fout.open("warehouse.txt");
    thread_local std::random_device rd;
    thread_local std::mt19937 engine(rd());
    thread_local std::uniform_int_distribution<unsigned char> dist1(10, 50);
    thread_local std::uniform_int_distribution<unsigned int> dist2(1000, 100000);
    unsigned char amount_of_units = dist1(engine);
    for(unsigned char i = 0; i < amount_of_units; i++){
        fout << generate_random_string() << " " << dist2(engine) << "\n";
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
    while(true) {
        std::unique_lock<std::mutex> locker(buf_mu);
        cond.wait(locker, [this]() { return st->size() < st_max_size; });
        st->push(newElement);
        locker.unlock();
        cond.notify_all();
        return;
    }
}

template<class T>
T Buffer<T>::remove() {
    while(true) {
        std::unique_lock<std::mutex> locker(buf_mu);
        cond.wait(locker, [this]() { return !st->empty(); });
        T top = st->top();
        st->pop();
        locker.unlock();
        cond.notify_all();
        return top;
    }
}

template<class T>
bool Buffer<T>::empty() const{
    return st->empty();
}

Producer::Producer(Buffer<std::string> *_units,
         Buffer<unsigned int> *_prices,
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
        units->add(unit);
        prices->add(price);
        std::unique_lock<std::mutex> locker(pr_con_mu);
        std::cout << "Unit \"" << unit << "\" of price " << price <<
        " is taken out of the warehouse\n";
        std::this_thread::sleep_for(std::chrono::milliseconds(50));
        locker.unlock();
    }
}

template<class T>
Consumer<T>::Consumer(const std::string& path) : overall_price(0){
    fout.open(path);
}

template<class T>
Consumer<T>::~Consumer() {
    if(typeid(T).name() == typeid(unsigned int).name()) {
        std::cout << "Overall price is " << overall_price << "\n";
        fout << "Overall price is " << overall_price << "\n";
    }
    objects = nullptr;
    fout.close();
}

template<class T>
void Consumer<T>::run() {
    while(!file_is_empty || !objects->empty()){
        if(!objects->empty()) {
            T unit = objects->remove();
            std::unique_lock<std::mutex> locker(pr_con_mu);
            if (typeid(T) == typeid(std::string)) {
                fout << unit << "\n";
                std::cout << "Unit \"" << unit << "\" is entrained in the truck\n";
                std::this_thread::sleep_for(std::chrono::milliseconds(50));
            } else if (typeid(T) == typeid(unsigned int)) {
                overall_price += *reinterpret_cast<unsigned int*>(&unit);
                fout << unit << "\n";
                std::cout << "+" << unit << " in price list\n";
                std::this_thread::sleep_for(std::chrono::milliseconds(50));
            }
            locker.unlock();
        }
    }
}

template<class T>
void Consumer<T>::get_buffer(const Producer *pr){
    if(typeid(T).name() == typeid(std::string).name()){
        objects = reinterpret_cast<Buffer<T> *>(pr->units);
    } else if (typeid(T).name() == typeid(unsigned int).name()){
        objects = reinterpret_cast<Buffer<T> *>(pr->prices);
    }
}
