## Building the Application

```bash
./mvnw clean install
```

## Running Tests

The project includes unit tests and integration tests. Run them using:

```bash
./mvnw test
```

## Running the Application

After successful build, you can run the application using:

```bash
./mvnw spring-boot:run
```

## Credentials

```
username: user.1@example.com
pasword: 123

username: user.2@example.com
pasword: 123
```

## Curl

```
Get all account from user.1@example.com

curl --request GET \
  --url http://localhost:8080/api/accounts/ \
  --header 'authorization: Basic dXNlci4xQGV4YW1wbGUuY29tOjEyMw=='
  
Withdraw from user.1@example.com

curl --request POST \
  --url http://localhost:8080/api/accounts/withdraw \
  --header 'authorization: Basic dXNlci4xQGV4YW1wbGUuY29tOjEyMw==' \
  --header 'content-type: application/json' \
  --data '{
  "account": 1,
  "amount": 100
}'

user.1@example.com credit account -> user.2@example.com debit account

curl --request POST \
  --url http://localhost:8080/api/accounts/transfer \
  --header 'authorization: Basic dXNlci4xQGV4YW1wbGUuY29tOjEyMw==' \
  --header 'content-type: application/json' \
  --data '{
  "sourceAccountId": 2,
  "targetAccountId": 3,
  "amount": 100
}'
```

### Please check AccountControllerIntegrationTest first, to make it easier to understand the code