[![Build Status](https://travis-ci.org/DevelopmentOnTheEdge/egisso-checker.svg?branch=master)](https://travis-ci.org/DevelopmentOnTheEdge/egisso-checker)

Чекер предназначен для проверки файлов с ЛКМСЗ (локальный классификатор мер социальной защиты) для первичной загрузки в ЕГИССО.

### Установка и запуск программы
Для установки чекера необходимо скачать этот jar файл:

[egisso-checker-0.1.1.jar](https://github.com/DevelopmentOnTheEdge/egisso-checker/releases/download/0.1.1/egisso-checker-0.1.1.jar)

Для запуска чекера на компьютере должна быть установлена Java (версия 8).

Для проверки .xml файла нужно выполнить команду:

`java -jar egisso-checker-0.1.1.jar файл_для_проверки.xml`

или

`java -jar egisso-checker-0.1.1.jar` 

для проверки всех файлов в текущей директории.

По результатам проверки для каждого проверяемого файла будут сформированы 2 дополнительных файла:
* файл\_для\_проверки.xml.prt - протокол с результатамии проверки, который содержит статистику по типам ошибок.
* файл\_для\_проверки.xml.err - перечень ошибок в текстовом формате (разделитель - tab).

### XML схема
Проверка осуществляется при помощи XML схемы 10.05.I-1.0.**3**.xsd (текущая последняя версия от ПФР).

Поэтому, проверяемый XML файл должен указывать именно эту схему, например:

````xml
<?xml version="1.0" encoding="utf-8"?>
<request
    xmlns="urn://egisso-ru/msg/10.05.I/1.0.3"
    xmlns:pac="urn://egisso-ru/types/package-LMSZ/1.0.2"
    xmlns:LMSZ="urn://egisso-ru/types/local-MSZ/1.0.2"
>
````

Если же будет указана схема 10.05.I-1.0.**2**.xsd, то в результате проверки будет ошибка.
Например, при проверке такого файла: 

````xml
<?xml version="1.0" encoding="utf-8"?>
<request
    xmlns="urn://egisso-ru/msg/10.05.I/1.0.2"
    xmlns:pac="urn://egisso-ru/types/package-LMSZ/1.0.2"
    xmlns:LMSZ="urn://egisso-ru/types/local-MSZ/1.0.2"
>
....
````

Файл с ошибками (.err) будет содержать следующую запись:
````
Строка  Позиция Код ошибки  Описание
6   2   cvc-elt.1   Невозможно найти объявление элемента 'request'.
````

## Тестовый пример

* test.xml - проверяемый файл. 

В него внесена ошибка - код периодичности указан как '011' вместо '01'.

````xml
<?xml version="1.0" encoding="UTF-8"?>
<em:request xmlns:etplmsz="urn://egisso-ru/types/package-LMSZ/1.0.2"
            xmlns:etlmsz="urn://egisso-ru/types/local-MSZ/1.0.2"
            xmlns:em="urn://egisso-ru/msg/10.05.I/1.0.3">
    <etplmsz:package>
        <etplmsz:packageID>43dfEcaf-d20D-E2eE-aaBa-A3eAd4f9a335</etplmsz:packageID>
        <etplmsz:elements>
            <etplmsz:localMSZ>
                <etlmsz:ID>bfC98fbd-3b9b-BdA4-Cbff-ebf1DdD8E2a6</etlmsz:ID>
                <etlmsz:code>1234</etlmsz:code>
                <etlmsz:title>Пособие по инвалидности</etlmsz:title>
                <etlmsz:dateEnact>2016-04-10</etlmsz:dateEnact>
                <etlmsz:dateExpiration>2020-02-05Z</etlmsz:dateExpiration>
                <!--etlmsz:periodicityCode>01</etlmsz:periodicityCode-->
                <etlmsz:periodicityCode>011</etlmsz:periodicityCode>
                <etlmsz:classificationKMSZ>
                    <etlmsz:codePartKMSZ>001000</etlmsz:codePartKMSZ>
                    <etlmsz:codeMSZ>2785</etlmsz:codeMSZ>
                    <etlmsz:codeProvisionForm>69</etlmsz:codeProvisionForm>
                    <etlmsz:codeLevelNPA>19</etlmsz:codeLevelNPA>
                    <etlmsz:localCategories>
                        <etlmsz:localCategory>
                            <etlmsz:ID>8e3207EC-6ECe-307B-Fa98-91FC4EdAbF53</etlmsz:ID>
                            <etlmsz:title>56465</etlmsz:title>
                            <etlmsz:codeCategoryKMSZ>89057682</etlmsz:codeCategoryKMSZ>
                        </etlmsz:localCategory>
                    </etlmsz:localCategories>
                    <etlmsz:cofinancing>
                        <etlmsz:fundingSource>
                            <etlmsz:codeFundingSource>1086</etlmsz:codeFundingSource>
                            <etlmsz:quota>100.00</etlmsz:quota>
                        </etlmsz:fundingSource>
                    </etlmsz:cofinancing>
                </etlmsz:classificationKMSZ>
                <etlmsz:territories>
                    <etlmsz:codeOKTMO>19043206</etlmsz:codeOKTMO>
                </etlmsz:territories>
                <etlmsz:reasons>
                    <etlmsz:NPA>
                        <etlmsz:number>654654</etlmsz:number>
                        <etlmsz:date>2016-11-07Z</etlmsz:date>
                        <etlmsz:title>О социальной защите инвалидов в Российской Федерации</etlmsz:title>
                        <etlmsz:authority>Государственная Дума</etlmsz:authority>
                        <etlmsz:URL>http://www.consultant.ru/document/cons_doc_LAW_8559/</etlmsz:URL>
                    </etlmsz:NPA>
                </etlmsz:reasons>
                <etlmsz:KBKCode>63678667868632242901</etlmsz:KBKCode>
                <etplmsz:lastChanging>2016-04-10T17:41:13Z</etplmsz:lastChanging>
            </etplmsz:localMSZ>
        </etplmsz:elements>
    </etplmsz:package>
</em:request>
````

* test.xml.prt - протокол проверки
````
Чекер для ЕГИССО, версия: 0.1.1
Проверяемый файл:  test.xml
Файл протокола:    test.xml.prt
Файл с ошибками:   test.xml.err
XSD схема для проверки локального классификатора услуг (ЛКМСЗ): 10.05.I-1.0.3.xsd

Статистика по ошибкам (число - описание ошибки):
1   - Значение '011' недопустимо из-за шаблона '\d{2}'.
1   - Значение '011' элемента 'etlmsz:periodicityCode' недопустимо.
````

* test.xml.err - описание ошибок
````
Строка  Позиция Код ошибки  Описание
15  69  cvc-pattern-valid   Значение '011' недопустимо из-за шаблона '\d{2}'.
15  69  cvc-type.3.1.3  Значение '011' элемента 'etlmsz:periodicityCode' недопустимо.
````
