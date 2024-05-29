import Head from 'next/head'
import styles from './page.module.css'

export default function Home() {
  return (
    <div className={styles.container}>
      <Head>
        <title>Jenkins Landing Page</title>
        <meta name="description" content="A minimalistic landing page for Jenkins" />
        <link rel="icon" href="/favicon.ico" />
      </Head>

      <header className={styles.header}>
        <img src="/jenkins-logo.png" alt="Jenkins Logo" className={styles.logo} />
        <h1 className={styles.title}>Bienvenido Santiago</h1>
        <p className={styles.description}>
          ¡En unos segundos comenzamos con la demostración!
        </p>
      </header>

      <main className={styles.main}>
      <section className={styles.section}>
        <h2 className={styles.subtitle}>¿Por qué Jenkins?</h2>
        <p className={styles.text}>
          Jenkins ofrece una manera sencilla de configurar un entorno de integración continua o entrega continua para casi cualquier combinación de lenguajes y repositorios de código fuente utilizando pipelines.
        </p>
      </section>

        <section className={styles.section}>
          <h2 className={styles.subtitle}>Integraciones</h2>
          <div className={styles.integrationGrid}>
            <div className={styles.integrationCard}>
              <img src="/jira.svg" alt="Jira Logo" className={styles.integrationLogo} />
              <h3 className={styles.integrationTitle}>Jira</h3>
              <p className={styles.integrationText}>
                Jira es una herramienta de gestión de proyectos que permite a los equipos planificar, rastrear y gestionar tareas y proyectos de manera efectiva.
              </p>
            </div>
            <div className={styles.integrationCard}>
              <img src="/slack.png" alt="Slack Logo" className={styles.integrationLogo} />
              <h3 className={styles.integrationTitle}>Slack</h3>
              <p className={styles.integrationText}>
                Slack es una plataforma de mensajería que facilita la comunicación en equipo mediante canales organizados y herramientas colaborativas.
              </p>
            </div>
            <div className={styles.integrationCard}>
              <img src="/gmail.webp" alt="Email Logo" className={styles.integrationLogo} />
              <h3 className={styles.integrationTitle}>Gmail</h3>
              <p className={styles.integrationText}>
                El email es una herramienta fundamental para la comunicación oficial y el seguimiento de notificaciones importantes en proyectos.
              </p>
            </div>
            <div className={styles.integrationCard}>
              <img src="/git.png" alt="GitHub Logo" className={styles.integrationLogo} />
              <h3 className={styles.integrationTitle}>GitHub</h3>
              <p className={styles.integrationText}>
                Un pull request en GitHub permite a los desarrolladores colaborar en el código, revisando cambios y discutiendo mejoras antes de fusionar contribuciones.
              </p>
            </div>
          </div>
        </section>
      </main>

      <footer className={styles.footer}>
        <p>&copy; 2024 Jenkins Project. All rights reserved.</p>
      </footer>
    </div>
  )
}