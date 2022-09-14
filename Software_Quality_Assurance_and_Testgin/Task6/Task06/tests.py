import unittest
import time
import json
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.common.action_chains import ActionChains
from selenium.webdriver.support import expected_conditions
from selenium.webdriver.support.wait import WebDriverWait
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.common.desired_capabilities import DesiredCapabilities


class Tests(unittest.TestCase):
    def setUp(self):
        self.driver = webdriver.Chrome()
        self.vars = {}
        self.change_user()
        self.action_chains = ActionChains(self.driver)

    def change_user(self, main_user=True):
        if main_user:
            self.email = 'niktatarinov@gmail.com'
            self.user_name = 'natatarinovBSE196'
            self.password = 'BSE196nat'
        else:
            self.email = 'nickyoleary@yandex.ru'
            self.user_name = 'natBSE196'
            self.password = 'BSE196natatarinov'

    def test_add_comment(self):
        self.driver.get('https://ruswizard.su/test')
        time.sleep(1)
        element = self.driver.find_element(By.XPATH, '//a[contains(text(),\'Комментариев\')]')
        self.driver.execute_script('window.scrollTo(arguments[0], arguments[1]);',
                                   element.location['x'], element.location['y'] - 250)
        time.sleep(1)
        self.driver.execute_script('arguments[0].click();', element)
        time.sleep(1)
        self.driver.find_element(By.ID, 'comment').send_keys('комментарий')
        self.driver.find_element(By.ID, 'author').send_keys('Никита')
        self.driver.find_element(By.ID, 'email').send_keys('natatarinov@edu.hse.ru')
        self.driver.find_element(By.ID, 'url').send_keys('https://ruswizard.su/test/')
        time.sleep(1)
        element = self.driver.find_element(By.ID, 'submit')
        self.driver.execute_script('window.scrollTo(arguments[0], arguments[1]);',
                                   element.location['x'], element.location['y'] - 250)
        time.sleep(1)
        self.driver.execute_script('arguments[0].click();', element)
        time.sleep(5)

    def test_add_bad_comment(self):
        self.driver.get('https://ruswizard.su/test')
        time.sleep(1)
        element = self.driver.find_element(By.XPATH, '//a[contains(text(),\'Комментариев\')]')
        self.driver.execute_script('window.scrollTo(arguments[0], arguments[1]);',
                                   element.location['x'], element.location['y'] - 250)
        time.sleep(1)
        self.driver.execute_script('arguments[0].click();', element)
        time.sleep(1)
        self.driver.find_element(By.ID, 'comment').send_keys('geek')
        self.driver.find_element(By.ID, 'author').send_keys('Никита')
        self.driver.find_element(By.ID, 'email').send_keys('natatarinov@edu.hse.ru')
        self.driver.find_element(By.ID, 'url').send_keys('https://ruswizard.su/test/')
        time.sleep(1)
        element = self.driver.find_element(By.ID, 'submit')
        self.driver.execute_script('window.scrollTo(arguments[0], arguments[1]);',
                                   element.location['x'], element.location['y'] - 250)
        time.sleep(1)
        self.driver.execute_script('arguments[0].click();', element)
        time.sleep(5)

    def test_log_in(self):
        self.driver.get('https://ruswizard.su/test/')
        time.sleep(1)
        element = self.driver.find_element(
            By.XPATH, '/html/body/div[2]/div/aside/div/div[2]/div[3]/div/nav/ul/li[2]/a')
        self.driver.execute_script('window.scrollTo(arguments[0], arguments[1]);',
                                   element.location['x'], element.location['y'] - 250)
        time.sleep(1)
        self.driver.execute_script('arguments[0].click();', element)
        time.sleep(1)
        self.driver.find_element(By.ID, 'user_login').send_keys(self.user_name)
        time.sleep(1)
        self.driver.find_element(By.ID, 'user_pass').send_keys(self.password)
        time.sleep(1)
        self.driver.execute_script('arguments[0].click();',
                                   self.driver.find_element(By.ID, 'wp-submit'))
        time.sleep(5)

    def test_add_note(self):
        self.change_user()
        self.test_log_in()

        # создание новой записи
        self.action_chains.move_to_element(self.driver.find_element(
            By.XPATH, '//*[@id="menu-posts"]/a/div[2]'))
        time.sleep(1)
        element = self.driver.find_element(By.XPATH, '//*[@id="menu-posts"]/ul/li[3]/a')
        self.action_chains.move_to_element(element)
        time.sleep(1)
        self.driver.execute_script('arguments[0].click();', element)
        time.sleep(1)

        # закрытие окна
        self.driver.execute_script('arguments[0].click();', self.driver.find_element(
            By.XPATH, '/html/body/div[4]/div/div/div/div/div/div[1]/button'))
        time.sleep(1)

        # редактирование содержимого
        # редактирование заголовка
        element = self.driver.find_element(By.XPATH, '//*[@id="post-title-0"]')
        self.driver.execute_script('arguments[0].click();', element)
        element.send_keys('заголовок')
        # добавление и редактирование абзаца
        time.sleep(1)
        self.driver.execute_script('arguments[0].click();', self.driver.find_element(
            By.XPATH, '//*[@id="editor"]/div[1]/div[1]/div[2]/div[2]/div[3]/'
                      'div[2]/div/div[2]/div[3]/div/div/div/button'))
        time.sleep(1)
        self.driver.execute_script('arguments[0].click();', self.driver.find_element(
            By.XPATH, '/html/body/div[1]/div[2]/div[2]/div[1]/div[2]/div[1]/div[2]/'
                      'div/div/div/div/div[2]/div[2]/div/div[1]/div[1]/button'))
        element = self.driver.find_element(By.XPATH, '//p')
        self.driver.execute_script('arguments[0].click();', element)
        element.send_keys('абзац')
        time.sleep(1)
        # добавление и редактирование внутреннего заголовка
        self.driver.execute_script('arguments[0].click();', self.driver.find_element(
            By.XPATH, '/html/body/div[1]/div[2]/div[2]/div[1]/div[2]/div[1]/'
                      'div[1]/div[1]/div[1]/div/div[1]/div/div/button[1]'))
        time.sleep(1)
        self.driver.execute_script('arguments[0].click();', self.driver.find_element(
            By.XPATH, '/html/body/div[1]/div[2]/div[2]/div[1]/div[2]/div[1]/div[1]/'
                      'div[1]/div[2]/div[1]/div/div[2]/div/div/div/div[2]/div[2]/'
                      'div[1]/div/div[2]/div/div[1]/div[3]/button'))
        element = self.driver.find_element(By.XPATH, '//h2/span')
        self.driver.execute_script('arguments[0].click();', element)
        self.action_chains.move_to_element(element).send_keys_to_element(element, 'заголовок')
        time.sleep(1)
        # добавление и редактирование изображения
        self.driver.execute_script('arguments[0].click();', self.driver.find_element(
            By.XPATH, '//*[@id="editor"]/div[1]/div[1]/div[2]/div[2]/div[3]/'
                      'div[3]/div/div[2]/div[3]/div/div/div/button'))
        time.sleep(1)
        self.driver.execute_script('arguments[0].click();', self.driver.find_element(
            By.XPATH, '/html/body/div[1]/div[2]/div[2]/div[1]/div[2]/div[1]/div[2]/'
                      'div/div/div/div/div[2]/div[2]/div/div[1]/div[5]/button'))
        self.driver.execute_script('arguments[0].click();', self.driver.find_element(
            By.XPATH, '/html/body/div[1]/div[2]/div[2]/div[1]/div[2]/div[1]/'
                      'div[1]/div[1]/div[2]/div[2]/div[3]/div[3]/div/div[2]/'
                      'div[3]/figure/div/div[3]/div[3]/button'))
        element = self.driver.find_element(
            By.XPATH, '//*[@id="editor"]/div[2]/div/div/div/div/div/form/input')
        self.driver.execute_script('arguments[0].click();', element)
        element.send_keys('https://www.culture.ru/storage/images/'
                          '401d79532c4a08ee97f1df06c89e6092/'
                          '0ff6dc53d876296726741c3b08552396.jpeg')
        element = self.driver.find_element(
            By.XPATH, '/html/body/div[1]/div[2]/div[2]/div[1]/div[2]/'
                      'div[1]/div[2]/div/div/div/div/div/form/button')
        self.driver.execute_script('arguments[0].click();', element)
        time.sleep(1)
        # добавление и редактирование цитаты
        self.driver.execute_script('arguments[0].click();', self.driver.find_element(
            By.XPATH, '//*[@id="editor"]/div[1]/div[1]/div[2]/div[2]/div[3]/'
                      'div[3]/div/div[2]/div[3]/div/div/div/button'))
        time.sleep(1)
        self.driver.execute_script('arguments[0].click();', self.driver.find_element(
            By.XPATH, '/html/body/div[1]/div[2]/div[2]/div[1]/div[2]/div[1]/div[2]/'
                      'div/div/div/div/div[2]/div[2]/div/div[2]/div[5]/button'))
        element = self.driver.find_element(By.XPATH, '//blockquote/div')
        self.driver.execute_script('arguments[0].click();', element)
        element.send_keys('цитата')
        element = self.driver.find_element(By.XPATH, '//cite')
        self.driver.execute_script('arguments[0].click();', element)
        element.send_keys('автор')
        time.sleep(1)

        # открытие настроек записи (меню справа)
        self.driver.execute_script('arguments[0].click();', self.driver.find_element(
            By.XPATH, '//*[@id="editor"]/div[1]/div[1]/div[1]/div/div[2]/div[2]/button'))
        self.driver.execute_script('arguments[0].click();', self.driver.find_element(
            By.XPATH, '//*[@id="editor"]/div[1]/div[1]/div[2]/'
                      'div[3]/div/div[2]/ul/li[1]/button'))
        # демонстрация постоянной ссылки
        self.driver.execute_script('arguments[0].click();', self.driver.find_element(
            By.XPATH, '//*[@id="editor"]/div[1]/div[1]/div[2]/'
                      'div[3]/div/div[3]/div[3]/h2/button'))
        # добавление меток
        self.driver.execute_script('arguments[0].click();', self.driver.find_element(
            By.XPATH, '//*[@id="editor"]/div[1]/div[1]/div[2]/'
                      'div[3]/div/div[3]/div[5]/h2/button'))
        element = self.driver.find_element(By.XPATH, '//*[@id="components-form-token-input-0"]')
        self.driver.execute_script('arguments[0].click();', element)
        element.send_keys('метка,')
        time.sleep(5)

        # публикация записи
        self.driver.execute_script('arguments[0].click();', self.driver.find_element(
            By.XPATH, '//*[@id="editor"]/div[1]/div[1]/div[1]/div/div[2]/button[2]'))
        self.driver.execute_script('arguments[0].click();', self.driver.find_element(
            By.XPATH, '//*[@id="editor"]/div[1]/div[1]/div[2]/div[4]/'
                      'div[2]/div/div/div[1]/div[1]/button'))
        time.sleep(1)

        # переход на главную страницу
        self.driver.get('https://ruswizard.su/test/')
        self.driver.implicitly_wait(5)
        try:
            self.driver.switch_to.alert.accept()
        except expected_conditions.NoAlertPresentException:
            pass
        time.sleep(5)

    def test_visible(self):
        self.driver.get('https://ruswizard.su/test/')
        time.sleep(5)
        self.change_user(False)
        self.test_log_in()
        self.driver.get('https://ruswizard.su/test/')
        time.sleep(5)

    def test_change_note(self):
        self.driver.get('https://ruswizard.su/test/')
        self.change_user()
        self.test_log_in()

        # открытие страницы с записями
        self.action_chains.move_to_element(self.driver.find_element(
            By.XPATH, '//*[@id="menu-posts"]/a/div[2]'))
        time.sleep(1)
        element = self.driver.find_element(
            By.XPATH, '//*[@id="menu-posts"]/ul/li[2]/a')
        self.action_chains.move_to_element(element)
        time.sleep(1)
        self.driver.execute_script('arguments[0].click();', element)
        time.sleep(1)

        # открытие записи для изменения
        self.driver.execute_script('arguments[0].click();', self.driver.find_element(
            By.XPATH, '/html/body/div[1]/div[2]/div[2]/div[1]/div[3]/'
                      'form[1]/table/tbody/tr/td[1]/div[3]/span[1]/a'))

        # закрытие окна
        self.driver.execute_script('arguments[0].click();', self.driver.find_element(
            By.XPATH, '/html/body/div[4]/div/div/div/div/div/div[1]/button'))
        time.sleep(1)

        # изменение видимости
        self.driver.execute_script('arguments[0].click();', self.driver.find_element(
            By.XPATH, '//*[@id="editor"]/div[1]/div[1]/div[2]/div[3]/'
                      'div/div[3]/div[1]/div[1]/div/button'))
        time.sleep(1)
        self.driver.execute_script('arguments[0].click();', self.driver.find_element(
            By.XPATH, '//*[@id="editor-post-private-0"]'))
        self.driver.implicitly_wait(5)
        try:
            self.driver.switch_to.alert.accept()
        except expected_conditions.NoAlertPresentException:
            pass
        time.sleep(5)

    def test_add_note_with_delay(self):
        self.change_user()
        self.test_log_in()

        # создание новой записи
        self.action_chains.move_to_element(self.driver.find_element(
            By.XPATH, '//*[@id="menu-posts"]/a/div[2]'))
        time.sleep(1)
        element = self.driver.find_element(By.XPATH, '//*[@id="menu-posts"]/ul/li[3]/a')
        self.action_chains.move_to_element(element)
        time.sleep(1)
        self.driver.execute_script('arguments[0].click();', element)
        time.sleep(1)

        # закрытие окна
        self.driver.execute_script('arguments[0].click();', self.driver.find_element(
            By.XPATH, '/html/body/div[4]/div/div/div/div/div/div[1]/button'))
        time.sleep(1)

        # редактирование заголовка
        element = self.driver.find_element(By.XPATH, '//*[@id="post-title-0"]')
        self.driver.execute_script('arguments[0].click();', element)
        element.send_keys('с задержкой')

        # перенос публикации на 5 минут вперёд
        self.driver.execute_script('arguments[0].click();', self.driver.find_element(
            By.XPATH, '//*[@id="editor"]/div[1]/div[1]/div[2]/div[3]/'
                      'div/div[3]/div[1]/div[2]/div/button'))
        element = self.driver.find_element(
            By.XPATH, '//*[@id="editor"]/div[2]/div/div/div/div/'
                      'div[1]/fieldset[2]/div/div/input[2]')
        self.driver.execute_script('arguments[0].click();', element)
        element.send_keys('\b\b' + str(int(element.get_attribute('value')) + 5))
        time.sleep(5)

        # публикация записи
        self.driver.execute_script('arguments[0].click();', self.driver.find_element(
            By.XPATH, '//*[@id="editor"]/div[1]/div[1]/div[1]/div/div[2]/button[2]'))
        self.driver.execute_script('arguments[0].click();', self.driver.find_element(
            By.XPATH, '//*[@id="editor"]/div[1]/div[1]/div[2]/div[4]/'
                      'div[2]/div/div/div[1]/div[1]/button'))
        time.sleep(1)

        # переход на главную страницу
        self.driver.get('https://ruswizard.su/test/')
        self.driver.implicitly_wait(5)
        try:
            self.driver.switch_to.alert.accept()
        except expected_conditions.NoAlertPresentException:
            pass
        time.sleep(5)

    def test_delete_notes(self):
        self.driver.get('https://ruswizard.su/test/')
        self.change_user()
        self.test_log_in()

        # открытие страницы с записями
        self.action_chains.move_to_element(self.driver.find_element(
            By.XPATH, '//*[@id="menu-posts"]/a/div[2]'))
        time.sleep(1)
        element = self.driver.find_element(
            By.XPATH, '//*[@id="menu-posts"]/ul/li[2]/a')
        self.action_chains.move_to_element(element)
        time.sleep(1)
        self.driver.execute_script('arguments[0].click();', element)
        time.sleep(5)

        # удаление записей
        self.driver.execute_script('arguments[0].click();', self.driver.find_element(
            By.XPATH, '/html/body/div[1]/div[2]/div[2]/div[1]/div[3]/form[1]/'
                      'table/tbody/tr[2]/td[1]/div[3]/span[3]/a'))
        self.driver.execute_script('arguments[0].click();', self.driver.find_element(
            By.XPATH, '/html/body/div[1]/div[2]/div[2]/div[1]/div[3]/form[1]/'
                      'table/tbody/tr[1]/td[1]/div[3]/span[3]/a'))
        time.sleep(5)

    def tearDown(self):
        self.driver.quit()


if __name__ == '__main__':
    unittest.main()
