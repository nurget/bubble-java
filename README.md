# bubble-java
졸업 프로젝트 - 시각장애인 및 저시력자 이동권 개선을 위한 버스 어플리케이션

###### 최종 수정일 : 2023-06-14

## 개요
- 작품명: 시각장애인 및 저시력자 이동권 개선을 위한 버스 어플리케이션

## 작품개발 동기 및 목적

- 👨‍🦯시각장애인이 혼자 버스를 탈 수 있을까?🚌 사회실험 | 실험카메라
1. 아래 사회실험을 시청 후 시각장애인 분들의 버스 이용에 불편함을 겪는 걸 알게 되었고, 이를 개선하고자 해당 작품을 개발하게 됨.
2. 음성인식 기술(TTS, STT) 사용.

<https://www.youtube.com/watch?v=jBrhnGL4WE0&ab_channel=%EC%9B%90%EC%83%B7%ED%95%9C%EC%86%94OneshotHansol>

## 작품 개발 목표 및 설계 요건

- 설계 목표 : 시각장애인 및 저시력자 이동권 개선을 위한 버스 어플리케이션

- 설계 요구사항
1. 정확한 음성인식 기술을 요구함.
2. 주 사용자들(시각장애인 및 저시력자)에게 맞는 GUI 설계가 필요함.

- 설계에 따른 현실적 제약요소
1. 앱 특성상 여러 시스템을 하나로 통합하는 과정이 기술적으로 어려울 것이라 예상됨.


## 작품 개발환경
|제목|설명|
|------|---|
|안드로이드 스튜디오|전반적인 앱 개발, 자바로 개발|
|Node.js|앱 서버 개발|
|MySQL|공공데이터를 기반으로 데이터베이스 설계 및 구축|
|Google Cloud|음성인식 기술(TTS, STT) API 구축|


## 구현 기능
1. 정류장 식별 기능  
   GPS 위치 기반, 실시간으로 사용자의 정류장 정보 제공
2. 정류장 예약 기능
   실시간으로 제공된 정류장 예약
3. 버스 예약 기능
   실시간으로 제공된 정류장에서 운행하는 버스 번호 예약 기능 제공

## 시스템 구성도

### 1. 개발환경이 포함된 시스템 구성도
<img width="100%" src="https://user-images.githubusercontent.com/101033246/225028757-e5207eda-e439-42a4-83c8-e19e0e5fa27c.png"/>

### 2. 도식화 한 시스템 구성도
<img width="100%" src="https://user-images.githubusercontent.com/101033246/224725803-414f7f07-9a53-49af-bfcd-bea767726794.png"/>
# bubble-java
