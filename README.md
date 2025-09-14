# Debezium HTTP Example

Projeto exemplo usando Debezium Embedded para capturar alterações de um banco MySQL e enviar via HTTP para um endpoint REST.

## Estrutura

- **Java 11+**
- **Maven**
- **Docker Compose** (MySQL + Adminer)
- **Debezium Embedded**

## Como rodar

### 1. Suba o banco MySQL com Docker

```sh
docker-compose up -d
```

- O banco estará acessível em `localhost:3306`
- Usuário: `debezium` / Senha: `debeziumpw`
- Banco: `testdb`
- Adminer para consulta rápida: [http://localhost:8081](http://localhost:8081)

### 2. Compile o projeto Java

```sh
mvn clean package
```

### 3. Execute o DebeziumToHttp

```sh
java -cp target/debezium-http-1.0-SNAPSHOT.jar DebeziumToHttp
```

Por padrão, toda alteração capturada será enviada para o endpoint:
```
http://localhost:8080/api/logs
```
Altere o código se quiser outro destino.

### 4. Crie tabelas e insira dados no banco

Acesse o Adminer e crie uma tabela, por exemplo:

```sql
CREATE TABLE pessoa (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255)
);

INSERT INTO pessoa (nome) VALUES ('João');
```

### 5. Teste o recebimento HTTP

Implemente um serviço HTTP de exemplo em Node, Python, etc, escutando em `/api/logs` para ver os dados chegando.

Exemplo com Node.js (Express):

```js
const express = require('express');
const app = express();
app.use(express.json());
app.post('/api/logs', (req, res) => {
  console.log(req.body);
  res.sendStatus(200);
});
app.listen(8080);
```

## Dicas

- Alterações em qualquer tabela do banco serão capturadas.
- O arquivo de histórico do Debezium será salvo em `/tmp/dbhistory.dat` (pode ser alterado).
- Para produção, ajuste permissões e variáveis conforme sua necessidade.

## Referências

- [Debezium Embedded](https://debezium.io/documentation/reference/stable/embedded/)
- [Debezium MySQL Connector](https://debezium.io/documentation/reference/stable/connectors/mysql.html)
- [Adminer](https://www.adminer.org/)