# Validação no emulador

Checklist executado no APK de depuração em um emulador Android real, além dos testes Robolectric. Não foram encontrados bloqueios nos fluxos executados nem registros no buffer de crashes do Android.

## Ambiente

- AVD exclusivo: `MNunesNails_API36_Test`, perfil Pixel 6.
- Android 16/API 36, imagem oficial AOSP x86_64, revisão 2.
- Aceleração WHPX e renderização SwiftShader; execução sem janela.
- Resolução padrão: 1080 × 2400; teste compacto: 720 × 1280, densidade 320 (360 × 640 dp).
- Interação por ADB com elementos identificados pelo XML do UI Automator; inspeção visual das capturas de tela.
- Aplicativo: `br.com.mnunesnails.financas`, APK `app/build/outputs/apk/debug/app-debug.apk`.

## Resultados

| Verificação | Resultado |
| --- | --- |
| Instalação limpa, estados vazios e navegação nas três telas | Passou |
| Serviço Manicure, R$ 45,00, descrição, duração 1h e categoria | Passou; dados conferidos no armazenamento privado |
| Nome vazio, zero e valor com três casas decimais | Salvamento bloqueado |
| Cancelamento de serviço e receita | Nenhum registro extra criado |
| Limites de descrição e observações | 201 caracteres limitados a 200, com contador correto |
| Receita sem serviço ou sem pagamento | Salvamento bloqueado |
| Receita com Pix e observação | Passou; valor e detalhes conferidos |
| Calendário e recebimento no mês anterior | Passou; data selecionada foi persistida |
| Resumo mensal e acumulado | R$ 45,00 no mês atual; R$ 90,00 acumulados; comparação de 0% com o mês anterior |
| Encerrar com `am force-stop` e reabrir | Serviço e duas receitas permaneceram salvos |
| Rotação com formulário preenchido | Rascunho preservado |
| Modo claro → escuro → claro | Cores atualizadas e rascunhos preservados |
| Detalhes de receita, menu, categoria e calendário no escuro | Legíveis e funcionais |
| Tela compacta, rolagem, teclado virtual e cancelamento | Passou |
| Botão Voltar | Retorna ao Início a partir das listagens |
| Buffer de crashes após o teste | Vazio |

Os registros usados existem somente no dispositivo virtual de teste. O APK continua sem dados de demonstração. O Android do emulador usa seu próprio fuso e idioma; o calendário nativo acompanha o idioma do dispositivo.

## Evidências locais

As 19 capturas e os dados de verificação ficam em `app/build/emulator-test/` (saída local de testes, ignorada pelo Git):

- `01-home-light.png`: início vazio no modo claro.
- `03-service-landscape.png`: formulário preenchido após rotação.
- `04-service-dark.png`: rascunho no modo escuro.
- `07-income-dark.png`: receita preenchida no modo escuro.
- `11-calendar-dark.png`: calendário no modo escuro.
- `12-home-dark-final.png`: resumo com registros nos dois meses.
- `15-home-light-final.png`: resumo no modo claro após reabrir o aplicativo.
- `19-keyboard-compact.png`: teclado virtual e formulário na tela compacta.
- `records.json`: registros de teste verificados.
- `crash-log.txt`: buffer de crashes do Android.

## Reproduzir

1. Compile o APK conforme o README.
2. Inicie o AVD `MNunesNails_API36_Test` na porta 5556, com Android totalmente inicializado.
3. Com Python 3 e `LOCALAPPDATA` configurados, execute `python scripts/run_emulator_check.py`.
4. Se o dispositivo já contiver os registros de uma execução anterior, utilize `python scripts/run_emulator_check.py --reset-test-data`. Essa opção limpa apenas os dados deste aplicativo no AVD de teste; o script valida o nome do AVD antes de prosseguir.

O script executa as etapas de serviços, receitas e verificações finais, mantendo capturas e evidências. As etapas individuais dependem do estado deixado pela anterior; prefira o comando principal. O teste cobre este emulador e as configurações descritas, não todos os aparelhos e versões Android suportados.
