#include <iostream>
#include <random>
#include <ctime>
#include <chrono>
#include <thread>
#include <mutex>

///Метод иллюстрации загрузки какого-либо процесса.
void loading() {
    std::this_thread::sleep_for(std::chrono::milliseconds(1000));
    std::cout << "---------------------" << std::endl;
    std::this_thread::sleep_for(std::chrono::milliseconds(1000));
    std::cout << "---------------------" << std::endl;
    std::this_thread::sleep_for(std::chrono::milliseconds(1000));
    std::cout << "---------------------" << std::endl;
    std::this_thread::sleep_for(std::chrono::milliseconds(1000));
}

///Класс симуляции работы парикмахера через многопоточность.
class hairdressing_salon {
private:
    ///Количество клиентов в очереди к парикмахеру.
    ///Равно 0 в начале рабочего дня.
    static size_t amount_of_clients;

    ///Отражает, открыт ли салон.
    ///Равно true, если салон открыт, и false
    ///в противном случае.
    static bool salon_is_open;

    ///Метод-поток, имитирующий работу парикмахера.
    static void hairdresser_thread();

    ///Метод-поток, имитирующий увеличение очереди.
    static void queue_thread();

public:
    ///Запускает симуляцию работы парикмахера.
    static void run_salon() {
        hairdresser_mu = new std::mutex();
        client_mu = new std::mutex();
        loading();
        //Клиентов изначально 0.
        amount_of_clients = 0;
        //Открываем салон.
        salon_is_open = true;
        std::cout << "Салон открылся. " << std::endl;
        std::this_thread::sleep_for(std::chrono::milliseconds(1000));
        //Запускаем поток очереди клиентов.
        std::thread queue(&hairdressing_salon::queue_thread);
        //Запускаем поток парикмахера.
        std::thread hairdresser(&hairdressing_salon::hairdresser_thread);
        //Дожидаемся окончания рабочего дня парикмахера.
        hairdresser.join();
        //Закрываем салон, как только закончился рабочий день парикмахера.
        salon_is_open = false;
        //Дожидаемся окончания потока очереди.
        queue.join();
        std::this_thread::sleep_for(std::chrono::milliseconds(1000));
        std::cout << "Салон закрылся. " << std::endl << "Необслуженными осталось " <<
                  amount_of_clients << " клиентов. " << std::endl;
        //Обнуляем количество человек в очереди (до следующего дня).
        amount_of_clients = 0;
        loading();
        delete hairdresser_mu;
        delete client_mu;
    }

private:
    ///Мьютекс для потока-парикмахера.
    static std::mutex *hairdresser_mu;
    ///Мьютекс для потока-клиента.
    static std::mutex *client_mu;
};

//Инициализация статических переменных.
size_t hairdressing_salon::amount_of_clients = 0;
bool hairdressing_salon::salon_is_open = false;
std::mutex* hairdressing_salon::hairdresser_mu = nullptr;
std::mutex* hairdressing_salon::client_mu = nullptr;

int main() {
    //Добавляем корректный вывод кириллицы в консоль.
    setlocale(LC_ALL, "Russian");
    do {
        std::cout << "Симуляция готова к запуску. Для её начала, введите \"запустить\". " <<
                  "Для завершения программы, введите любую другую строку. " << std::endl;
        //Выбор пользователя (запустить симуляцию или выйти).
        std::string choice;
        std::cin >> choice;
        if (choice != "запустить") {
            break;
        }
        //Запуск симуляции.
        hairdressing_salon::run_salon();
        std::cout << "Симуляция завершена. Перезагрузка. " << std::endl;
        loading();
    } while (true);
    std::cout << "Завершение программы. " << std::endl;
    loading();
    std::cout << "Программа завершена. " << std::endl;
    return 0;
}

void hairdressing_salon::hairdresser_thread() {
    //Генератор длительности стрижки.
    thread_local std::random_device rd;
    thread_local std::mt19937 engine(rd());
    thread_local std::uniform_int_distribution<unsigned short> haircut_duration(450, 550);
    //Начало рабочего дня.
    time_t start = time(nullptr);
    //Начинаем рабочий день.
    while (time(nullptr) - start < 10) {
        std::unique_lock<std::mutex> locker(*hairdresser_mu);
        //Парикмахер ждёт кличентов.
        if(!amount_of_clients){
            std::cout << "Парикмахер спит в ожидании клиента. " << std::endl;
        }
        while (amount_of_clients == 0);
        std::cout << "Парикмахер принял очередного посетителя. " << std::endl;
        amount_of_clients--;
        locker.unlock();
        //Идёт стрижка.
        std::this_thread::sleep_for(std::chrono::milliseconds(haircut_duration(engine)));
        locker.lock();
        std::cout << "Парикмахер постриг очередного клиента. " << std::endl;
        locker.unlock();
    }
}

void hairdressing_salon::queue_thread() {
    //Генератор ожидания очередного клиента.
    thread_local std::random_device rd;
    thread_local std::mt19937 engine(rd());
    thread_local std::uniform_int_distribution<unsigned short> client_probability(0, 750);
    //Очередь растёт, пока салон открыт.
    while (salon_is_open) {
        std::this_thread::sleep_for(std::chrono::milliseconds(client_probability(rd)));
        std::unique_lock<std::mutex> locker(*client_mu);
        std::cout << "В очередь встал ещё 1 клиент. " << std::endl;
        amount_of_clients++;
        locker.unlock();
    }
}