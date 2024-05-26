
<img src="docs/img/jenkins.svg" alt="jenkins logo" width="180" style="display:block;margin:auto">

# CI/CD

El siguiente repositorio contiene proyecto y un tutorial realizado para demostrar el funcionamiento de Jenkins para implementar CI/CD. El proyecto fue realizado como parte del TPE para la materia _Redes de Información_ por el grupo 18, integrado por:
- [Axel Facundo Preiti Tasat](https://github.com/AxelPreitiT) (62618)
- [Gastón Ariel Francois](https://github.com/francoisgaston) (62500)
- [Tomás Camilo Gay Bare](https://github.com/tgaybare) (62103)
- [José Rodolfo Mentasti](https://github.com/JoseMenta) (62248)

## Configuración
Para configurar el servidor y el repositorio, se recomienda seguir el siguiente orden:
1. Configurar un repositorio para integrar con Jenkins. Se recomienda usar los contenidos de este mismo proyecto, que contienen una página web de prueba con tests.
2. Configurar el servidor de Jenkins con [este documento](docs/Jenkins.md)
3. Configurar el pipeline para el proyecto con [este documento](docs/Pipeline.md)
4. Configurar un pipeline en el proyecto, pudiendo usar los ejemplos de [este documento](docs/MainPipeline.md). También se puede agregar el pipeline para cada pull request que se explica en [este documento](docs/PrPipeline.md)
5. Configurar el ambiente de ejecución para el proyecto siguiendo [este documento](docs/Deploy.md)