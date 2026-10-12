from emulator_ui import *

assert records() == {'services': [], 'incomes': []}
tap('Salvar', 'content-desc')
assert not records()['services']
print('PASS: nome vazio bloqueado', flush=True)
fill('Nome do serviço', 'Manicure')
fill('Valor do serviço', '0')
tap('Salvar', 'content-desc', scroll=True)
assert not records()['services']
fill('Valor do serviço', '45.999')
tap('Salvar', 'content-desc', scroll=True)
assert not records()['services']
print('PASS: zero e mais de duas casas decimais bloqueados', flush=True)
fill('Valor do serviço', '45.00')
fill('Descrição (opcional)', 'Teste no emulador')
fill('Duração (opcional)', '1h')
adb('shell', 'settings', 'put', 'system', 'accelerometer_rotation', '0')
adb('shell', 'settings', 'put', 'system', 'user_rotation', '1')
time.sleep(1)
assert find('Nome do serviço', 'content-desc').get('text') == 'Manicure'
screenshot('03-service-landscape')
print('PASS: rascunho preservado na rotacao', flush=True)
adb('shell', 'settings', 'put', 'system', 'user_rotation', '0')
adb('shell', 'cmd', 'uimode', 'night', 'yes')
time.sleep(1)
assert find('Nome do serviço', 'content-desc').get('text') == 'Manicure'
assert find('Valor do serviço', 'content-desc').get('text') == '45.00'
screenshot('04-service-dark')
tap('Selecione uma categoria')
screenshot('05-category-dark')
tap('Manicure')
adb('shell', 'cmd', 'uimode', 'night', 'no')
time.sleep(1)
assert find('Descrição (opcional)', 'content-desc').get('text') == 'Teste no emulador'
tap('Salvar', 'content-desc', scroll=True)
service = records()['services'][0]
assert service['name'] == 'Manicure' and service['cents'] == 4500
assert service['duration'] == '1h' and service['category'] == 'Manicure'
assert service['description'] == 'Teste no emulador'
find('Serviços cadastrados: 1')
print('PASS: temas claro/escuro, categoria e cadastro persistido', flush=True)
tap('Cadastrar Serviço', 'content-desc')
fill('Nome do serviço', 'Cancelar teste')
fill('Descrição (opcional)', 'a'*201)
assert len(find('Descrição (opcional)', 'content-desc').get('text')) == 200
find('200/200')
tap('Cancelar', 'content-desc', scroll=True)
assert len(records()['services']) == 1
print('PASS: limite 200 caracteres e cancelamento de servico', flush=True)
tap('Registrar Receita', 'content-desc')
screenshot('06-income-light')
inspect()
