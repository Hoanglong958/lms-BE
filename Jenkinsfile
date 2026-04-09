pipeline {
    agent any

    environment {
        IMAGE_NAME = 'lms-backend'
        CONTAINER_NAME = 'backend'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "docker build -t ${IMAGE_NAME}:latest ."
            }
        }

        stage('Deploy') {
            steps {
                script {
                    // Nếu bạn quản lý deploy qua 1 file docker-compose.yml nằm chung ở server (ví dụ /home/minh/lms):
                    // Bỏ comment 2 dòng dưới đây để dùng docker-compose thay vì docker run
                    
                    /*
                    sh "cd /home/minh/lms && docker-compose build backend"
                    sh "cd /home/minh/lms && docker-compose up -d --no-deps backend"
                    */

                    // Mặc định (chạy standalone cho backend):
                    sh "docker rm -f ${CONTAINER_NAME} || true"
                    sh """
                    docker run -d \\
                        --name ${CONTAINER_NAME} \\
                        -p 3900:3900 \\
                        --add-host=host.docker.internal:host-gateway \\
                        -e SPRING_DATASOURCE_URL='jdbc:mysql://host.docker.internal:3306/lms_rikkei?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh' \\
                        -e OLLAMA_BASE_URL='http://host.docker.internal:11434' \\
                        ${IMAGE_NAME}:latest
                    """
                }
            }
        }
    }
}
