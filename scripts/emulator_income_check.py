from emulator_ui import *

tap('Salvar', 'content-desc')
assert not records()['incomes']
tap('Selecione o serviço')
tap('Manicure')
assert find('Valor recebido', 'content-desc').get('text') == '45,00'
tap('Salvar', 'content-desc')
assert not records()['incomes']
print('PASS: receita exige servico e forma de pagamento', flush=True)
tap('Selecione a forma de pagamento')
tap('Pix')
fill('Observações (opcional)', 'b'*201)
assert len(find('Observações (opcional)', 'content-desc').get('text')) == 200
find('200/200')
fill('Observações (opcional)', 'Recebimento de teste')
adb('shell', 'cmd', 'uimode', 'night', 'yes')
time.sleep(1)
assert find('Valor recebido', 'content-desc').get('text') == '45,00'
assert find('Observações (opcional)', 'content-desc').get('text') == 'Recebimento de teste'
find('Pix')
screenshot('07-income-dark')
print('PASS: limite de observacoes e rascunho da receita no modo escuro', flush=True)
tap('Salvar', 'content-desc')
assert records()['incomes'][0]['cents'] == 4500
assert records()['incomes'][0]['payment'] == 'Pix'
assert records()['incomes'][0]['notes'] == 'Recebimento de teste'
screenshot('08-home-dark')
adb('shell', 'am', 'force-stop', PACKAGE)
adb('shell', 'am', 'start', '-n', PACKAGE+'/.MainActivity')
time.sleep(1)
find('Serviços cadastrados: 1')
assert len(records()['incomes']) == 1
print('PASS: receita gravada e dados mantidos apos encerrar o processo', flush=True)
tap('Receitas', 'content-desc')
find('Manicure')
screenshot('09-receipts-dark')
tap('Manicure')
find('FECHAR')
screenshot('10-receipt-details-dark')
tap('FECHAR')
adb('shell', 'input', 'keyevent', 'KEYCODE_BACK')
find('Olá!')
tap('Registrar Receita', 'content-desc')
tap('Selecione o serviço')
tap('Manicure')
tap('Selecione a forma de pagamento')
tap('Dinheiro')
current = adb('shell','date','+%d/%m/%Y')
tap(current)
screenshot('11-calendar-dark')
inspect()

