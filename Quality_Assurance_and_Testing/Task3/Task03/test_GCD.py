from unittest import TestCase
import random
import pytest
from GCD import GCD


def fibonacci(n):
    x = 1
    y = 1
    for i in range(2, n):
        y += x
        x = y - x
    return y


class TestGCD(TestCase):

    test_obj = GCD()

    def check_gcd(self, x, y):
        gcd = self.test_obj.gcd(x, y)
        if x == 0 or y == 0:
            if gcd != max(abs(x), abs(y)):
                self.fail()
            return
        if x % gcd != 0 or y % gcd != 0 or gcd < 0:
            self.fail()
        for i in range(gcd + 1, min(abs(x), abs(y)) + 1):
            if x % i == 0 and y % i == 0:
                self.fail()

    # Положительные значения аргументов
    def test_gcd1(self):
        for i in range(0, 10):
            self.check_gcd(random.randint(1, 1000000), random.randint(1, 1000000))

    # Отрицательное значение первого, второго, обоих аргументов
    def test_gcd2(self):
        for i in range(0, 9):
            if i % 3 == 0:
                self.check_gcd(random.randint(-1000000, -1), random.randint(1, 1000000))
            elif i % 3 == 1:
                self.check_gcd(random.randint(1, 1000000), random.randint(-1000000, -1))
            else:
                self.check_gcd(random.randint(-1000000, -1), random.randint(-1000000, -1))

    # Нулевое значение первого, второго, обоих аргументов
    def test_gcd3(self):
        for i in range(0, 10):
            if i % 2 == 0:
                self.check_gcd(0, random.randint(1, 1000000))
                self.check_gcd(0, random.randint(-1000000, -1))
            else:
                self.check_gcd(random.randint(1, 1000000), 0)
                self.check_gcd(random.randint(-1000000, -1), 0)
        self.check_gcd(0, 0)

    # Неединичные взаимно простые аргументы
    def test_gcd4(self):
        for i in range(0, 10):
            assert self.test_obj.gcd(2 ** random.randint(1, 11), 3 ** random.randint(1, 11)) == 1

    # Равные значения аргументов
    def test_gcd5(self):
        for i in range(0, 10):
            x = random.randint(-1000000, 1000000)
            assert self.test_obj.gcd(x, x) == abs(x)

    # Неравные значения аргументов, при которых первый делит второй, второй делит первый
    def test_gcd6(self):
        for i in range(0, 10):
            if i % 2 == 0:
                x = random.randint(-1000000, 1000000)
                y = x * random.randint(2, 10)
            else:
                y = random.randint(-1000000, 1000000)
                x = y * random.randint(2, 10)
            assert self.test_obj.gcd(x, y) == min(abs(x), abs(y))

    # Неравные значения аргументов, дающие неединичный наибольший общий делитель
    def test_gcd7(self):
        assert self.test_obj.gcd(-21, 60) == 3
        assert self.test_obj.gcd(49, 84) == 7
        assert self.test_obj.gcd(42, -72) == 6
        assert self.test_obj.gcd(-16, -120) == 8

    # Граничные значения аргументов
    def test_gcd8(self):
        assert self.test_obj.gcd(-2 ** 31, 2 ** 31 - 1) == 1
        # Для Python не актуален, так как у целых чисел нет ограничения в 4 байта.

    # Аргументы - соседние числа Фибоначчи для большого n
    def test_gcd9(self):
        for i in range(0, 10):
            n = random.randint(10000, 20000)
            assert self.test_obj.gcd(fibonacci(n), fibonacci(n + 1)) == 1
