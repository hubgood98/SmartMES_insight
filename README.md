# SmartMES Insight

> Spring Boot 기반의 스마트팩토리 설비 이상 탐지 및 생산 실적 연동 시스템

![Java](https://img.shields.io/badge/Java-17-blue.svg)
![Spring Boot](https://img.shields.io/badge/SpringBoot-3.x-brightgreen.svg)
![Status](https://img.shields.io/badge/Progress-In%20Development-yellow.svg)

---

## 🔍 프로젝트 소개

**SmartMES Insight**는 공장의 설비 데이터를 실시간으로 수집하고, 기준 임계치를 초과하는 이상 상황을 자동 감지하며, 생산 실적을 체계적으로 연동·관리하는 시스템입니다.

> 📌 포트폴리오 및 스마트팩토리 관련 기업 지원용 백엔드 프로젝트

---

## ⚙️ 주요 기능

- 설비 및 센서 관리 (Facility & Sensor Management)
- 실시간 센서 데이터 수집 및 이상 감지
- WebSocket 기반 실시간 알람 전송
- 생산 작업 지시서 관리 및 실적 등록
- Batch 스케줄링을 통한 정기 통계 처리
- RESTful API 제공 + Spring Security 인증 구조
- 운영 상태 점검용 Actuator 포함

---

## 🧱 기술 스택

| 분류 | 사용 기술 |
|------|----------|
| 언어 | Java 17 |
| 프레임워크 | Spring Boot, Spring Data JPA, Spring Security, Spring Batch, WebSocket |
| 데이터베이스 | MySQL, MyBatis |
| 개발 도구 | IntelliJ, Gradle |
| 배포/운영 | Spring Actuator, GitHub |
| 기타 | Lombok, Validation, Scheduling API |

---

## 🗃️ ERD (주요 테이블)

- `User`, `Facility`, `Sensor`, `SensorLog`, `Alert`, `WorkOrder`, `ProductionResult`, `LogEntry`

> 👉 [ERD 보기 (dbdiagram.io 링크 예정)](#)

---

---

## 🧪 실행 방법

1. 프로젝트 클론
    ```bash
    git clone https://github.com/hubgood98/smartmes-insight.git
   cd smartmes-insight
    ```

2. DB 설정 변경 (application.yml)
3. 빌드 및 실행
    ```bash
    ./gradlew bootRun
    ```

---

## 🙋‍♂️ 개발자

| 이름  | 깃허브                    |
|-----|------------------------|
| 김희준 | [github.com/hubgood98](https://github.com/hubgood98) |

---

## 📌 License

MIT License © 2025