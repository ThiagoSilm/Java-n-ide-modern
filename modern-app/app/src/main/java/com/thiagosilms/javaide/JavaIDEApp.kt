package com.thiagosilms.javaide

import android.app.Application
import android.util.Log
import androidx.core.content.ContextCompat
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class JavaIDEApp : Application() { // ATENÇÃO: Mantenha o nome consistente!

    // Exemplo: Injetando uma dependência de configuração global
    // @Inject lateinit var appConfig: AppConfig

    companion object {
        private const val TAG = "JavaIDEApp"
    }

    /**
     * Chamado quando o processo da aplicação é criado.
     * Pode ser chamado múltiplas vezes se a app usar múltiplos processos.
     */
    override fun onCreate() {
        super.onCreate()

        // Inicializar apenas para o processo principal da UI
        if (isMainProcess()) {
            initializeForMainProcess()
        } else {
            // Processo de serviço/worker (ex.: compilação em background)
            Log.d(TAG, "App criada em processo de worker: ${getProcessName()}")
        }
    }

    /**
     * Inicializações que devem ocorrer apenas no processo principal de UI.
     */
    private fun initializeForMainProcess() {
        Log.i(TAG, "Inicializando Java IDE Modern - Processo Principal")

        // 1. Configurar tratamento global de exceções (CRÍTICO)
        setupGlobalExceptionHandler()

        // 2. Inicializar componentes do núcleo do IDE
        initializeCoreComponents()

        // 3. Verificar ambiente e permissões básicas (opcional)
        // checkEnvironment()

        // 4. Futuro: Inicializar Analytics, Crash Reporting, etc.
        // Firebase.initialize(this)
    }

    /**
     * Captura exceções não tratadas em toda a aplicação.
     * Impede crashes silenciosos e permite log.
     */
    private fun setupGlobalExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            // Logar o erro de forma estruturada
            Log.e(TAG, "Crash não tratado na thread: ${thread.name}", throwable)

            // Exemplo: Enviar para um serviço de crash reporting
            // crashReporter.logException(throwable)

            // Opcional: Mostrar feedback amigável ao usuário (na próxima inicialização)

            // Chamar o handler padrão (finaliza o app)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    /**
     * Inicializa componentes centrais que são usados globalmente.
     * Garante que estejam prontos antes da primeira Activity.
     */
    private fun initializeCoreComponents() {
        // Exemplos de inicializações:
        // - Configurar o caminho do SDK do Android/Java
        // - Inicializar cache de templates de código
        // - Carregar configurações de tema padrão
        // - Verificar licenças de plugins (se houver)

        try {
            // Exemplo: Garantir que diretórios de trabalho internos existam
            val filesDir = applicationContext.filesDir
            val projectsDir = android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_DOCUMENTS
            ).resolve("JavaIDEProjects")
            if (!projectsDir.exists()) {
                projectsDir.mkdirs()
            }
            Log.d(TAG, "Diretório de projetos: $projectsDir")
        } catch (e: SecurityException) {
            Log.w(TAG, "Sem permissão para acessar armazenamento externo", e)
        }
    }

    /**
     * Verifica se estamos no processo principal da aplicação.
     */
    private fun isMainProcess(): Boolean {
        return applicationInfo.packageName == getProcessName()
    }

    /**
     * Obtém o nome do processo atual.
     */
    private fun getProcessName(): String? {
        return ContextCompat.getMainExecutor(this).execute {
            // Esta chamada pode precisar ser adaptada dependendo do contexto
            // Uma alternativa é via ActivityThread para versões mais antigas
            android.os.Process.myPid().let { pid ->
                android.os.Process.getProcessName(pid)
            }
        }
        return null // Simplificação; implementação real requer mais código.
    }
}
