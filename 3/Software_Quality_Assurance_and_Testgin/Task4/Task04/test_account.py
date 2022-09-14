from unittest import TestCase
import random

from account import Account


class TestAccount(TestCase):
    def test2(self):
        account = Account()
        assert not account.is_blocked()
        assert account.withdraw(500)
        assert account.get_balance() == -500
        assert not account.withdraw(500)
        assert account.get_balance() == -500

    def test3(self):
        account = Account()
        assert not account.is_blocked()
        for i in range(0, 10):
            assert not account.set_max_credit(random.randint(-1000000, 1000000))
            assert account.get_max_credit() == 1000

    def test4(self):
        account = Account()
        account.block()
        assert account.is_blocked()
        assert not account.set_max_credit(1000001)
        assert account.get_max_credit() == 1000
        assert not account.set_max_credit(-1000001)
        assert account.get_max_credit() == 1000

    def test5(self):
        account = Account()
        account.block()
        assert account.is_blocked()
        for i in range(0, 10):
            new_max_credit = random.randint(-1000000, 1000000)
            assert account.set_max_credit(new_max_credit)
            assert account.get_max_credit() == new_max_credit

    def test6(self):
        account = Account()
        account.block()
        assert account.is_blocked()
        assert account.set_max_credit(-500)
        assert account.get_max_credit() == -500
        assert not account.unblock()
        assert account.is_blocked()
        assert account.get_balance() == 0

        assert account.set_max_credit(1)
        assert account.get_max_credit() == 1
        assert account.unblock()
        assert not account.is_blocked()
        assert account.get_balance() == 0

    def test7(self):
        account = Account()
        assert account.deposit(999999)
        assert account.get_balance() == 999999
        assert account.withdraw(999999)
        assert account.get_balance() == 0
        for i in range(0, 10):
            assert not account.deposit(random.randint(-1000000000, -1))
            assert account.get_balance() == 0
            assert not account.deposit(random.randint(1000000, 1000000000))
            assert account.get_balance() == 0
            assert not account.withdraw(random.randint(-1000000000, -1))
            assert account.get_balance() == 0
            assert not account.withdraw(random.randint(1000000, 1000000000))
            assert account.get_balance() == 0

    def test8(self):
        account = Account()
        account.block()
        assert account.set_max_credit(1000000)
        assert account.get_max_credit() == 1000000
        assert account.unblock()
        assert account.get_balance() == 0

        assert account.deposit(999999)
        assert account.get_balance() == 999999
        assert not account.deposit(1)
        assert account.get_balance() == 999999

        assert account.withdraw(999999)
        assert account.get_balance() == 0
        assert account.withdraw(999999)
        assert account.get_balance() == -999999
        assert not account.withdraw(1)
        assert account.get_balance() == -999999

    def test9(self):
        account = Account()
        account.block()
        assert account.is_blocked()
        for i in range(0, 10):
            assert not account.deposit(random.randint(0, 999999))
            assert account.get_balance() == 0
            assert not account.withdraw(random.randint(0, 999999))
            assert account.get_balance() == 0
