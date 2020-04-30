```bash
$ mvn jetty:run
```

```bash
$ http localhost:8080/app/foo
HTTP/1.1 200 OK
Content-Length: 13
Content-Type: application/octet-stream
Date: Thu, 30 Apr 2020 12:04:57 GMT
Server: Jetty(9.4.24.v20191120)

Hello, world!

$
```