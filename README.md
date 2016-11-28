# PEAPOD
Implementation of PEAPOD paper (Attribute Based Publishing with Hidden Credentials by Kapadia et al.) in the Java programming language with the Spring boot web framework and Redis in memory database as a backend.

1) Building:

   make

2) Running:

   ./bin/spring.sh

3) Testing:

    http://localhost:8080/send?user=Alice&body=Hello

    http://localhost:8080/recv?user=Bob

