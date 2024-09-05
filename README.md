# Relatório do Projeto: Implementação de um Microsserviço Reativo

## 1. Introdução

Este relatório detalha a implementação de um microsserviço reativo utilizando Spring Boot, Spring WebFlux, e Spring Data JDBC. O projeto segue os princípios da programação reativa e incorpora as melhores práticas para o desenvolvimento de microsserviços, incluindo containerização com Docker e implantação no Kubernetes.

## 2. Tecnologias Utilizadas

- Java 21
- Spring Boot 3.1.0
- Spring WebFlux
- Spring Data JDBC
- PostgreSQL
- Flyway
- Maven
- Lombok
- Testcontainers
- Docker
- Kubernetes

### Justificativas para as escolhas tecnológicas:

1. **Java 21**: Escolhemos a versão mais recente do Java para aproveitar as últimas melhorias de desempenho e recursos da linguagem.

2. **Spring Boot e Spring WebFlux**: Oferecem um modelo de programação reativa, permitindo o desenvolvimento de aplicações não-bloqueantes e escaláveis.

3. **Spring Data JDBC**: Proporciona uma camada de abstração para operações de banco de dados, mantendo a simplicidade e o controle sobre as consultas SQL.

4. **PostgreSQL**: Um banco de dados robusto e confiável, adequado para aplicações em produção.

5. **Flyway**: Facilita a gestão e versionamento de esquemas de banco de dados.

6. **Maven**: Um sistema de build maduro e amplamente utilizado na comunidade Java.

7. **Lombok**: Reduz a verbosidade do código Java, aumentando a produtividade.

8. **Testcontainers**: Permite a execução de testes de integração com dependências reais em containers Docker.

9. **Docker e Kubernetes**: Facilitam a containerização e orquestração da aplicação, permitindo implantações consistentes e escaláveis.

## 3. Estrutura do Projeto

Descrição da estrutura:
- `src/main/java`: Contém o código-fonte da aplicação
- `src/main/resources`: Contém arquivos de configuração e migrações do banco de dados
- `src/test`: Contém os testes da aplicação
- `kubernetes`: Contém os arquivos de configuração do Kubernetes
- `Dockerfile`: Define como a aplicação é containerizada

## 4. Implementação

### 4.1 Modelo de Dados

Implementamos uma entidade `Product`:

```java
@Data
@Table("products")
public class Product {
    @Id
    private Long id;
    private String name;
    private Double price;
}
```

### 4.2 Persistência

Utilizamos Spring Data JDBC para a camada de persistência:

```java
public interface ProductRepository extends CrudRepository<Product, Long> {
}
```

### 4.3 Serviço

O `ProductService` implementa a lógica de negócios:

```java
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public Flux<Product> getAllProducts() {
        return Flux.defer(() -> Flux.fromIterable(productRepository.findAll()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    // Outros métodos...
}
```

Observação: Utilizamos `Flux.defer()` e `Schedulers.boundedElastic()` para envolver operações bloqueantes em um contexto reativo.

### 4.4 Controller

O `ProductController` expõe os endpoints RESTful:

```java
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public Flux<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    // Outros endpoints...
}
```

### 4.5 Configuração do WebClient

Configuramos um `WebClient` para fazer requisições HTTP reativas:

```java
@Configuration
public class WebClientConfig {
    @Value("${api.base-url}")
    private String apiBaseUrl;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(apiBaseUrl)
                .build();
    }
}
```

## 5. Testes

Implementamos testes de integração usando Testcontainers:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = {ProductControllerTest.Initializer.class})
public class ProductControllerTest {

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("productdb")
            .withUsername("postgres")
            .withPassword("password");

    // Testes...
}
```

## 6. Containerização

Criamos um Dockerfile para containerizar a aplicação:

```dockerfile
# Build stage
FROM maven:3.8.4-openjdk-21-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM openjdk:21-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
```

## 7. Configuração do Kubernetes

Criamos arquivos de configuração para deployment e service no Kubernetes:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: reactive-microservice
spec:
  replicas: 3
  # ...

---

apiVersion: v1
kind: Service
metadata:
  name: reactive-microservice
spec:
  type: LoadBalancer
  # ...
```

## 8. Exemplos de Uso

### 8.1 Criar um Produto

Requisição:
```
POST http://localhost:8080/products
Content-Type: application/json

{
  "name": "Smartphone",
  "price": 999.99
}
```

Resposta:
```json
{
  "id": 1,
  "name": "Smartphone",
  "price": 999.99
}
```

### 8.2 Listar Produtos

Requisição:
```
GET http://localhost:8080/products
```

Resposta:
```json
[
  {
    "id": 1,
    "name": "Smartphone",
    "price": 999.99
  },
  {
    "id": 2,
    "name": "Laptop",
    "price": 1499.99
  }
]
```

## 9. Conclusão

Este projeto demonstra a implementação de um microsserviço reativo utilizando tecnologias modernas e práticas recomendadas. A aplicação é escalável, resiliente e pronta para ser implantada em um ambiente de nuvem.

Principais conquistas:
1. Implementação de endpoints reativos com Spring WebFlux
2. Integração com banco de dados PostgreSQL usando Spring Data JDBC
3. Testes de integração com Testcontainers
4. Containerização da aplicação com Docker
5. Configuração para implantação no Kubernetes
