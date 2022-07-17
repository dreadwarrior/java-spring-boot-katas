# Spring Boot Katas

## greetings

- simple greetings resource for bootstrapping testing
- demonstrates BDD-style testing

## multipartfileuploads

### TODO

- [ ] fix "java.net.SocketException: Broken pipe (Write failed)" (http/multipartfileuplodas.http#multiple files exceeding max request size)
- [ ] test / visualize metrics with Prometheus
- [ ] refactor `UploadSizeExceptionAdvice#recordMetrics()`

### knowledge resources

- [How to use `RestTemplate` for multipart file uploads](https://github.com/eugenp/tutorials/blob/master/spring-web-modules/spring-resttemplate-3/src/main/java/com/baeldung/web/upload/client/MultipartFileUploadClient.java)
- [How to use `ClassPathResource` in Spring JUnit tests](https://www.baeldung.com/spring-classpath-file-access)
- [Data units conversion](https://www.gbmb.org/)
- [Behavior-Driven Development mit JUnit 5 (german)](https://blog.codecentric.de/2018/09/behavior-driven-development-mit-junit-5/)
- [Spring Security Auto Configuration](https://docs.spring.io/spring-security/reference/servlet/getting-started.html#servlet-hello-auto-configuration)
- [How to disable CSRF for Spring Security](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html#servlet-csrf-configure-disable)
- [Multipart form requests with IntelliJ HTTP client](https://www.jetbrains.com/help/idea/exploring-http-syntax.html#use-multipart-form-data)

### Learnings

#### Spring Security CSRF configuration defaults

The Spring documentation is very detailed about CSRF protection in conjunction 
with [Multipart file uploads](https://docs.spring.io/spring-security/reference/features/exploits/csrf.html#csrf-considerations-multipart).

It also includes a section about [When to use CSRF protection](https://docs.spring.io/spring-security/reference/features/exploits/csrf.html#csrf-when)
which may help to make a decision about enabling or disabling it.

For the purpose of the `multipartfileuploads` use case, the CSRF protection was
disabled.

#### `spring.servlet.multipart.max-request-size` evaluation

When configuring the total request size for multipart requests, it may not be 
obvious that the request becomes too large even if multiple files are uploaded 
whose total size is equal to that setting.

A multipart file upload consists of additional data (like `--boundary--` 
markers; HTTP headers for each part e.g. `Content-Type: application/octet-stream`)
which raises the request content length, too.

#### Consider `Apache Commons Fileupload`

Using `commons-fileupload:commons-fileupload` may provide ["maximum portability 
across Servlet containers"](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-multipart-resolver-commons).

Note that both options – servlet built-in and commons-fileupload resolvers – 
make use of temporary files, which may not be feasible in a microservice 
architecture.

### How to...

#### ...generate sample files with certain size

    # replace "1153433" with desired size and adjust file name in `of` argument accordingly
    dd if=/dev/urandom bs=1153433 count=1 of=filegt1mb
