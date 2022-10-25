# Spring Boot Katas

## Requirements

* JDK 17
* Maven 3.8

## Katas

### Greetings

- simple greetings resource for bootstrapping testing
- demonstrates BDD-style testing

### Multi-part File Uploads

- practice emitting metrics about errors on incoming multi-part file uploads

#### TODO

- [ ] fix "java.net.SocketException: Broken pipe (Write failed)" (http/multipartfileuploads.http#multiple files exceeding max request size)
- [ ] test / visualize metrics with Prometheus
- [ ] refactor `UploadSizeExceptionAdvice#recordMetrics()`

#### Development

1. Start the `Application`
2. Note down the generated security password from the log messaging starting 
   with `Using generated security password`
3. Run the following command to create a IntelliJ HTTP client configuration

   ```sh
   cp -n src/test/http/http-client.env.json http-client.private.env.json`
   ```

4. Insert the generated security password of step 2. to the JSON property at 
   path `dev.password`
5. Open the file `multipartfileuploads.http`, select the `dev` environment in
   field **Run with**.
6. Run the examples

Hot reloading the application (<kbd>Cmd</kbd>+<kbd>F9</kbd>) during development
is possible, as Spring Boot Devtools are available in the classpath. Steps 2. + 4. 
need to be re-run in this case.

#### Run tests

1. Generate payload samples (generates 512KB, 1MB, 1.1MB files):

    ```sh
    dd if=/dev/urandom bs=524288 count=1 of=src/test/resources/multipartfileuploads/file512kb
    dd if=/dev/urandom bs=1048576 count=1 of=src/test/resources/multipartfileuploads/file1mb
    dd if=/dev/urandom bs=1153433 count=1 of=src/test/resources/multipartfileuploads/filegt1mb
    ```

2. Run the use-case test:

    ```sh
    mvn -Dtest="*.multipartfileuploads.**" test
    ```

#### Knowledge resources

- [How to use `RestTemplate` for multipart file uploads](https://github.com/eugenp/tutorials/blob/master/spring-web-modules/spring-resttemplate-3/src/main/java/com/baeldung/web/upload/client/MultipartFileUploadClient.java)
- [How to use `ClassPathResource` in Spring JUnit tests](https://www.baeldung.com/spring-classpath-file-access)
- [Data units conversion](https://www.gbmb.org/)
- [Behavior-Driven Development mit JUnit 5 (german)](https://blog.codecentric.de/2018/09/behavior-driven-development-mit-junit-5/)
- [Spring Security Auto Configuration](https://docs.spring.io/spring-security/reference/servlet/getting-started.html#servlet-hello-auto-configuration)
- [How to disable CSRF for Spring Security](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html#servlet-csrf-configure-disable)
- [Multipart form requests with IntelliJ HTTP client](https://www.jetbrains.com/help/idea/exploring-http-syntax.html#use-multipart-form-data)

#### Learnings

##### Spring Security CSRF configuration defaults

The Spring documentation is very detailed about CSRF protection in conjunction 
with [Multipart file uploads](https://docs.spring.io/spring-security/reference/features/exploits/csrf.html#csrf-considerations-multipart).

It also includes a section about [When to use CSRF protection](https://docs.spring.io/spring-security/reference/features/exploits/csrf.html#csrf-when)
which may help to make a decision about enabling or disabling it.

For the purpose of the `multipartfileuploads` use case, the CSRF protection was
disabled.

##### `spring.servlet.multipart.max-request-size` evaluation

When configuring the total request size for multipart requests, it may not be 
obvious that the request becomes too large even if multiple files are uploaded 
whose total size is equal to that setting.

A multipart file upload consists of additional data (like `--boundary--` 
markers; HTTP headers for each part e.g. `Content-Type: application/octet-stream`)
which raises the request content length, too.

##### Consider `Apache Commons Fileupload`

Using `commons-fileupload:commons-fileupload` may provide ["maximum portability 
across Servlet containers"](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-multipart-resolver-commons).

Note that both options – servlet built-in and commons-fileupload resolvers – 
make use of temporary files, which may not be feasible in a microservice 
architecture.

#### How to...

##### ...generate sample files with certain size

    # replace "1153433" with desired size and adjust file name in `of` argument accordingly
    dd if=/dev/urandom bs=1153433 count=1 of=filegt1mb
