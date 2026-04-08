# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **Smart Home-Based Elderly Care Service Management Platform** (智慧居家养老服务管理平台) for government oversight and service provider management in the elderly care industry.

## System Architecture

### Multi-Level User Hierarchy
The system follows a 5-level administrative hierarchy:
1. **Municipal Government** (市级) - Top-level oversight and configuration
2. **District/County** (区/县级) - Regional management
3. **Street/Town** (街道/乡镇) - Local coordination
4. **Community** (社区) - Front-line service coordination
5. **Service Provider** (服务商) - Service delivery

### Key Functional Modules

| Module | Description |
|--------|-------------|
| **Service Provider Management** (服务商管理) | Provider registration, authentication, service scope management, personnel assignments |
| **Service Personnel Management** (服务人员管理) | Personnel records, credentials, service types, service areas |
| **Order Management** (订单管理) | Order lifecycle: scheduling → dispatching → service delivery → evaluation → settlement |
| **Elderly/Client Management** (老人/客户管理) | Client profiles, care levels, government subsidies, health records |
| **Financial Settlement Management** (财务结算管理) | Service pricing, subsidy calculations, payment processing, reconciliation |
| **Service Assessment Evaluation** (服务评估评价) | Service quality scoring, client feedback, dispute handling |
| **System Configuration** (系统配置) | Data dictionaries, service types, pricing policies, area management |
| **Data Statistics** (数据统计) | Service volume reports, financial summaries, performance metrics |

### Service Types

The platform supports multiple elderly care services:
- In-home care (生活照料) - bathing, feeding, personal hygiene
- Day care (日间照料) - community-based daily care
- Meal delivery (助餐服务) - food delivery to elderly
- Cleaning services (助洁服务) - home cleaning
- Laundry services (助浴服务/助洗服务) - bathing and laundry assistance
- Health monitoring (健康管理) - blood pressure monitoring, medication reminders
- Rehabilitation assistance (康复护理) - rehabilitation exercises, medical assistance
- Mental health support (精神慰藉) - companionship, psychological support
- Information consultation (信息咨询) - policy consultation, service information
- Emergency assistance (紧急救援) - emergency response services

## Data Integration Sources

The system integrates with multiple existing government systems:
- **Civil Affairs** (民政) - elderly information, government subsidies
- **Social Security** (社保) - medical insurance, pension data
- **Health/Family Planning** (卫计) - health records, medical information
- **Business/Commerce** (工商) - business registration for providers
- **Disabled Persons Federation** (残联) - disability information

## Project Status

This project is currently in the **planning phase**. The repository contains:
- Specification document (PDF): `智慧居家养老服务管理平台V20230707(3).pdf`
- UI reference materials: screenshots and design mockups
- **Complete requirements documentation** (10 documents in `docs/requirements/`):
  - Requirements overview and 8 subsystem specifications
  - Total: ~150,000 words, covering 100+ functional modules
  - 100+ data models, 50+ RESTful APIs

No source code has been implemented yet.

## Requirements Documentation

Detailed requirements specifications are available in the `docs/requirements/` directory:

- `README.md` - Document navigation and quick reference
- `01-需求规格说明书总览.md` - Overall requirements overview
- `02-服务商管理子系统需求规格说明书.md` - Service provider management
- `03-服务人员管理子系统需求规格说明书.md` - Service personnel management
- `04-订单管理子系统需求规格说明书.md` - Order management
- `05-老人客户管理子系统需求规格说明书.md` - Elderly/client management
- `06-财务结算管理子系统需求规格说明书.md` - Financial settlement management
- `07-服务评估评价子系统需求规格说明书.md` - Service assessment and evaluation
- `08-系统配置子系统需求规格说明书.md` - System configuration
- `09-数据统计分析子系统需求规格说明书.md` - Data statistics and analysis

Refer to these documents for detailed business logic, functional requirements, and data models.

## Development Notes

### Order Lifecycle Flow
```
Client Request → Service Provider Acceptance → Personnel Assignment →
Service Delivery → Service Evaluation → Order Settlement → Payment Processing
```

### Permission Model
- Each level can only view and manage data within their jurisdiction
- Municipal level has full oversight and configuration rights
- Community and provider levels have operational rights limited to their assigned clients and personnel

### Service Pricing Structure
Services support:
- Government subsidy rates (government-funded)
- Self-pay rates (client-funded)
- Mixed pricing (subsidy + self-pay)

## Documentation

Refer to the specification PDF for detailed business logic, functional requirements, and UI/UX specifications.
