# Java N-IDE - Modernizado

Um IDE Android moderno e poderoso para desenvolvimento Java/Kotlin com recursos avançados de teste.

![Logo do Java N-IDE](assets/images/logo.png)

## ✨ Recursos Principais

- 🧪 Sistema avançado de testes
- 📊 Análise de cobertura em tempo real
- 🔄 Modo de observação contínua
- 🐞 Depuração integrada
- 📱 Interface Material Design 3
- 🔌 Modo offline completo
- 📈 Métricas e insights

## 🚀 Começando

### Pré-requisitos

- Android Studio ou IntelliJ IDEA
- JDK 17 ou superior
- Gradle 8.0+
- Android SDK 34+

### Instalação

1. Clone o repositório:
```bash
git clone https://github.com/shenghuntianlang/java-n-ide-upgraded.git
```

2. Abra o projeto na sua IDE:
```bash
cd java-n-ide-upgraded
./gradlew assembleDebug
```

3. Execute o aplicativo em um dispositivo ou emulador

## 📖 Guia de Uso

### Sistema de Testes

1. Execução Rápida:
```kotlin
testingAPI.quickTest("MeuArquivo.kt")
```

2. Configuração Personalizada:
```kotlin
testingAPI.runTests(TestConfig(
    includeUnitTests = true,
    includeCoverage = true
))
```

3. Modo de Observação:
```kotlin
testingAPI.watchTests(config) { result ->
    // Atualizar UI
}
```

### Análise de Cobertura

- Mapa de calor interativo
- Métricas detalhadas
- Sugestões de melhoria
- Navegação direta ao código

### Depuração

- Pontos de parada inteligentes
- Análise de variáveis
- Stack trace aprimorado
- Histórico de execução

## 🤝 Contribuindo

1. Faça um Fork do projeto
2. Crie sua Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a Branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está licenciado sob a Licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

## 🙏 Agradecimentos

- Projeto original Java N-IDE
- Comunidade Android
- Contribuidores

## 📱 Screenshots

![Dashboard](assets/images/dashboard.png)
![Testes](assets/images/tests.png)
![Cobertura](assets/images/coverage.png)

## 📞 Contato

Email: suporte@javanide.dev
Twitter: @JavaNIDEApp
