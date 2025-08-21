# Instagram Clone

Este é um projeto de um clone simples do Instagram utilizando Docker, MariaDB, Spring Boot para o backend e NextJS com Tailwind CSS para o frontend. O objetivo deste projeto é permitir que vocês desenvolvam testes unitários para as funcionalidades implementadas.

## Pré-requisitos

Antes de começar, verifique se você tem as seguintes ferramentas instaladas em sua máquina:

- Git
- Docker
- Docker Compose

## Passos para executar o projeto

### 1. Fork e clone o repositório

Primeiramente, acesse `https://github.com/ppereiradev/instagram-clone` e faça um **fork** do projeto para o seu Github.

Depois, clone o repositório para a sua máquina local:

```bash
git clone https://github.com/seu-github-username/instagram-clone.git
cd instagram-clone
```

### 2. Suba os containers Docker

Execute o seguinte comando para iniciar os containers do Docker com o backend (Spring Boot), frontend (NextJS) e o banco de dados (MariaDB):

```bash
docker compose up -d
```

### 3. Executando o Backend

Para rodar o backend (Spring Boot) dentro do container Docker, siga os passos abaixo:

1. Entre no terminal do container do backend:

```bash
docker container exec -it instagram-clone-backend bash
```

2. Execute o backend utilizando o Maven:

```bash
mvn clean spring-boot:run
```

Agora, o backend estará em execução.

### 4. O Frontend executa automaticamente
O frontend executa automanticamente, não há necessidade de acessar o terminal e executar nenhum comando.

Você pode acessar a aplicação frontend no navegador, indo para `http://localhost:3000`.

Para visualizar os logs e analisar se houve algum erro na execução do frontend, execute o comando abaixo:

```bash
docker container logs -f instagram-clone-frontend
```

### 5. Executando os Testes Unitários do Backend

Para executar os testes unitários no backend, execute o comando Maven abaixo dentro do container do backend:

```bash
mvn clean test
```

Os testes unitários devem ser executados corretamente, garantindo que as funcionalidades do backend estão funcionando conforme esperado.

### 6. Conclusão

<span style="color:lightgreen">**LEMBRE-SE DE SALVAR TODO O SEU PROGRESSO NO SEU GITHUB**.</span>

Este é o ambiente completo para o desenvolvimento do clone simples do Instagram. O próximo passo é realizar a implementação dos testes unitários, com base nas funcionalidades presentes no backend, e garantir que o código esteja funcionando de acordo com as expectativas.

---

## Descrição do Desafio

Você recebeu um projeto inicial de um clone simples do Instagram, contendo as funcionalidades básicas de **Sign In** e **Sign Up** implementadas na interface gráfica, gerenciadas pelo `AuthController`. O projeto também inclui endpoints para operações CRUD no `UserController`.

Seu desafio é **implementar testes unitários e de integração** neste projeto, garantindo a qualidade e a confiabilidade do código.

#### Tecnologias Utilizadas no Projeto
- **Backend**: Spring Boot
- **Frontend**: Next.js com Tailwind CSS
- **Banco de Dados**: MariaDB
- **Ambiente**: Docker
- **Bibliotecas para Testes**: JUnit, Mockito, DataJpaTest, MockMvc

---

### Tarefas do Desafio

1. **Testes Unitários**
   - Escreva testes unitários para as classes:
     - **`UserServiceImpl`**: Use **JUnit** e **Mockito** para mockar dependências e validar o comportamento esperado.
     - **`JwtUtils`**: Teste as funcionalidades de manipulação de tokens JWT, como geração e validação de tokens.

2. **Testes de Integração com o Banco de Dados**
   - Teste a integração entre a interface **`UserRepository`** e o banco de dados usando **JUnit** e **DataJpaTest**.
   - Garanta que as operações de CRUD no repositório estejam funcionando corretamente.

3. **Testes de Integração de Controladores**
   - Use **MockMvc** para testar a integração completa entre os controladores (**`AuthController`** e **`UserController`**) e o restante do sistema.
   - Valide as respostas dos endpoints, os status HTTP retornados e a lógica das rotas implementadas.

---

### Critérios de Avaliação
1. **Cobertura de Código**
   - O código será avaliado com base na cobertura dos testes. Quanto maior a cobertura, melhor será sua avaliação.
   - Use ferramentas como Jacoco para gerar relatórios de cobertura.

2. **Organização e Clareza dos Testes**
   - Os testes devem ser bem organizados, com nomes que reflitam o comportamento testado.
   - O uso de boas práticas, como a configuração e o teardown adequados, será levado em consideração.

3. **Uso das Ferramentas de Teste**
   - Demonstre domínio sobre JUnit, Mockito, DataJpaTest e MockMvc.
   - Utilize mocks adequadamente nos testes unitários e configure corretamente os testes de integração.

---

### Dicas e Recomendações
- Consulte a documentação oficial de **JUnit**, **Mockito**, **DataJpaTest** e **MockMvc** para resolver dúvidas.
- Divida o trabalho em etapas: escreva e valide os testes unitários antes de passar para os testes de integração.
- Garanta que todos os testes passem antes de entregar o desafio.
- Explore os arquivos de configuração no projeto para entender como as dependências estão configuradas.
- Salve todo seu progresso em commits e faça o push no seu Github.

Bom trabalho e boa sorte no desafio!
