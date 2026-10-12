# MNunes Nails Finanças

Aplicativo Android nativo em Kotlin para acompanhar serviços e receitas do salão.

## Abrir no Android Studio

1. Abra esta pasta como projeto e aguarde a sincronização Gradle.
2. Use o JDK 17 ou 21 no Gradle. Neste ambiente, o projeto usa `C:/Users/Carlos/.jdks/jbr-21.0.11`; o JBR 25 do Android Studio não é compatível com este Gradle.
3. Instale o Android SDK 36 e configure o SDK em `local.properties` (arquivo local, ignorado pelo Git).
4. Execute o módulo `app` em emulador ou dispositivo com Android 8.0/API 26 ou superior.

No PowerShell, com JAVA_HOME configurado:

```powershell
./gradlew.bat assembleDebug lintDebug testDebugUnitTest
```

O APK é gerado em `app/build/outputs/apk/debug/app-debug.apk`.

## Sprint 1 — Estruturação e Interface

- [x] Base do projeto Android Studio: Kotlin, SDK 36, mínimo API 26, Gradle Wrapper.
- [x] Estrutura do repositório e arquivos locais/build ignorados pelo Git.
- [x] Estrutura inicial: Activity para interface e navegação; pacote `data` para modelos e repositório.
- [x] Tela inicial com total de receitas do mês, quantidade de atendimentos e serviços cadastrados.
- [x] Telas de serviços e receitas, com estados vazios e formulários.
- [x] Navegação inferior entre Início, Serviços e Receitas; voltar retorna à tela inicial.

## Comportamento

Cadastre um serviço com nome e preço e, opcionalmente, descrição, duração e categoria. Em Receitas, selecione um serviço, confirme o valor recebido, escolha a data e a forma de pagamento e inclua observações, se necessário. A data começa no dia atual e o valor sugerido pode ser alterado. O aplicativo aceita vírgula ou ponto decimal, exige valores positivos e armazena dinheiro em centavos. Os registros ficam no armazenamento privado do aplicativo e permanecem após reiniciar. Não há dados de demonstração.

O visual segue a referência fornecida: fundo claro, detalhes em rosa, logotipo floral, cartões de resumo, ações rápidas, últimos lançamentos e navegação inferior. Os formulários abrem em tela cheia e preservam o rascunho ao girar o dispositivo. Os ícones são vetoriais, desenhados em Canvas. O painel mostra receitas do mês, total acumulado, quantidade de atendimentos e comparação com o mês anterior quando há uma base de comparação. Registros antigos continuam disponíveis; os novos campos opcionais são lidos como vazios.

Esta sprint não inclui edição/exclusão, despesas, autenticação, sincronização ou backup. Desinstalar o aplicativo remove os registros locais.

O aplicativo acompanha automaticamente o modo claro/escuro do Android, com paletas próprias para fundos, cartões, texto, campos, ícones e barras do sistema. Menus e seleção de data também acompanham o tema. Ao alternar o tema do sistema com um formulário aberto, a tela e o rascunho são restaurados.

## Verificação manual

1. Em uma instalação limpa, verificar os estados vazios nas três telas.
2. Cadastrar Manicure por 45,00 e verificar a listagem de serviços.
3. Registrar uma receita desse serviço, selecionando a forma de pagamento; verificar data, valor e total do mês no Início. Alterar a data para o mês anterior e verificar a comparação mensal.
4. Tentar salvar nome vazio, zero e valores com mais de duas casas decimais; o formulário deve indicar o erro.
5. Cancelar um formulário; não deve criar registros.
6. Fechar e abrir o app para verificar persistência; girar o dispositivo para verificar a aba selecionada e os rascunhos dos formulários.
7. Navegar pelas três abas e usar Voltar para retornar ao Início.
8. Preencher os campos opcionais, salvar e tocar no registro para conferir os detalhes. Conferir os contadores de 200 caracteres e tentar salvar uma receita sem serviço ou forma de pagamento.
9. Alternar o modo claro/escuro nas configurações do Android. Conferir as três telas, os dois formulários, menus e calendário. Alternar novamente com um formulário preenchido e confirmar que o rascunho permanece.

## Validação automatizada

Compilação do APK e lint concluídos sem erros. A suíte possui 10 testes aprovados: 2 para valores monetários e 8 testes de fluxo com Robolectric em Android 14/API 34 simulado. Os testes de fluxo verificam estados vazios, navegação, cadastro e validação de serviços, campos opcionais, recebimentos e pagamento obrigatório, seleção de data do mês anterior e comparação mensal, cancelamento, limite de 200 caracteres, leitura de registros antigos e restauração da tela e dos rascunhos. Também verificam a troca de orientação e de tema com um rascunho aberto.

Os testes usam armazenamento isolado e não adicionam dados de demonstração ao APK. A suíte Robolectric verifica a persistência reabrindo a Activity e o repositório. O checklist também foi executado no emulador Android 16/API 36, incluindo encerramento real do processo, troca de tema, rotação, teclado, menus, calendário e tela compacta. Consulte o [relatório do emulador](docs/emulator-validation.md) para resultados, evidências e reprodução.

Após executar o comando acima, os relatórios ficam em `app/build/reports/tests/testDebugUnitTest/index.html` e `app/build/reports/lint-results-debug.html`.

