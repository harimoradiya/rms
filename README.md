# 🍽️ Restaurant Management System API 1.0

<p align="center">
  <img src="docs/images/rms-logo.png" alt="RMS Logo" width="200"/>
  <br>
  <i>A modern, comprehensive REST API for restaurant management</i>
</p>

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![API Docs](https://img.shields.io/badge/API-Documentation-blue)](http://localhost:8080/swagger)
[![Kotlin](https://img.shields.io/badge/kotlin-1.9.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Ktor](https://img.shields.io/badge/ktor-3.0.1-blue.svg)](https://ktor.io/)

## 🌟 Features

### 👥 User Management
- Role-based authentication (Admin, Manager, Staff, Customer)
- JWT-based secure authentication
- User registration and login

### 🪑 Table Management
- Real-time table status tracking
- Table reservation system
- Session-based ordering

### 🍕 Order Management
- Create and manage orders
- Real-time order tracking
- Special instructions support
- Session-based billing

### 👨‍🍳 Kitchen Display System
- Real-time order notifications
- Order status updates
- Priority-based queue management
- Estimated preparation time

### 💳 Payment Processing
- Multiple payment methods (Cash, Card, UPI)
- Split billing support
- Table-wise payment tracking
- Payment status monitoring

### 📊 Analytics & Reporting
- Sales analytics
- Popular items tracking
- Peak hour analysis
- Revenue reports

### 📝 Feedback System
- Customer ratings
- Detailed feedback collection
- Analytics on customer satisfaction
- Service quality monitoring

### 📦 Inventory Management
- Stock tracking
- Low stock alerts
- Wastage monitoring
- Supplier management


## 📱 Mobile App Integration

The API is designed to work seamlessly with mobile applications:
- RESTful endpoints
- JWT authentication
- JSON responses
- Real-time updates support
- File upload/download support

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Authors

- **Hari Moradiya** - *Initial work* - [harimoradiya](https://github.com/harimoradiya)

## 🙏 Acknowledgments

- Ktor Framework
- PostgreSQL
- Swagger UI
- All contributors

## 📞 Support

For support, email support@restrofy.com or join our Slack channel.

## 🗺️ Roadmap

- [ ] Real-time notifications
- [ ] Advanced analytics dashboard
- [ ] Multi-language support
- [ ] Mobile app development
- [ ] Cloud deployment support

## 🐳 Docker Deployment

### Prerequisites
- Docker
- Docker Compose

### Steps to Deploy

1. Clone the repository:
```bash
git clone https://github.com/harimoradiya/rms.git
cd rms
```

2. Configure environment variables:
- Copy `.env.example` to `.env`
- Update the values in `.env` file

3. Build and start containers:
```bash
docker-compose up --build
```

4. Access the application:
- API: http://localhost:8080
- Swagger Documentation: http://localhost:8080/swagger

### Docker Commands

```bash
# Start containers in background
docker-compose up -d

# Stop containers
docker-compose down

# View logs
docker-compose logs -f

# Rebuild containers
docker-compose up --build

# Remove volumes (database data)
docker-compose down -v
```

### Production Deployment

For production deployment:
1. Update environment variables with production values
2. Use a proper database password
3. Configure SSL/TLS
4. Set up monitoring and logging
5. Use container orchestration (Kubernetes/Docker Swarm)