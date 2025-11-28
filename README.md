# 나라장터 입찰공고 검색 배치 프로그램

나라장터 API를 사용하여 입찰공고를 검색하고 결과를 이메일로 전송하는 배치 프로그램입니다.

## 설정 방법

### 1. 로컬 메일 서버 설치 (선택사항)

이메일 전송을 위해 로컬 메일 서버가 필요합니다. Postfix를 사용하는 경우:

```bash
# Postfix 설치 (Ubuntu/Debian)
sudo apt install postfix

# 설치 중 "Internet Site" 선택
# System mail name: localhost 또는 도메인 입력

# Postfix 시작
sudo systemctl start postfix
sudo systemctl enable postfix

# 상태 확인
sudo systemctl status postfix
```

**참고**: 로컬 메일 서버가 없어도 프로그램은 실행되지만, 이메일 전송은 실패합니다.

### 2. 조회 조건 설정 (application.yml)

```yaml
nara:
  search:
    keyword: 교통카드            # 검색할 키워드 (단일 또는 쉼표로 구분)
    days: 10                     # 조회 기간 (일)
    recipient: jhypark@lgcns.com # 수신 이메일 (단일 또는 쉼표로 구분)
```

**복수 키워드 및 수신자 설정**
```yaml
nara:
  search:
    keyword: 교통카드,버스,지하철         # 여러 키워드 검색
    days: 10
    recipient: user1@lgcns.com,user2@lgcns.com  # 여러 수신자
```

- 키워드: 쉼표(,)로 구분하여 여러 키워드 검색 가능 (결과는 자동 중복 제거)
- 수신자: 쉼표(,)로 구분하여 여러 명에게 동일한 결과 전송

## 실행 방법

### Gradle 사용
```bash
./gradlew build
./gradlew bootRun
```

### JAR 파일 직접 실행
```bash
./gradlew build
java -jar build/libs/nara-0.0.1-SNAPSHOT.jar
```

## 프로그램 동작

1. application.yml에서 설정한 조회 조건으로 나라장터 API 호출
2. 최근 N일간의 입찰공고 중 키워드가 포함된 공고 검색
3. 검색 결과를 HTML 형식으로 이메일 전송
4. 자동 종료

## 주요 기능

- **나라장터 검색조건 API 사용**: PPS(Public Procurement Service) 엔드포인트 사용
  - 물품 (getBidPblancListInfoThngPPSSrch)
  - 용역 (getBidPblancListInfoServcPPSSrch)
  - 공사 (getBidPblancListInfoCnstwkPPSSrch)
  - 외자 (getBidPblancListInfoFrgcptPPSSrch)
- **게시일시 기준 정렬**: 최신 공고가 먼저 표시
- **HTML 이메일**: 보기 좋은 형식으로 이메일 전송
- **설정 파일 기반**: 코드 수정 없이 조회 조건 변경 가능

## 로그 확인

실행 중 로그는 콘솔에 출력됩니다:
```
=== 나라장터 입찰공고 조회 배치 프로그램 시작 ===
검색 키워드: 교통카드
조회 기간: 최근 10 일
수신 이메일: greatacme@gmail.com
검색 결과: 5 건
이메일 전송 완료: greatacme@gmail.com (5 건)
=== 배치 프로그램 완료 ===
```

## 주의사항

- 로컬 메일 서버(Postfix 등)가 설치되어 있어야 이메일 전송이 가능합니다
- 메일 서버가 없어도 프로그램은 실행되지만 이메일 전송은 실패합니다
- 이메일 전송에 실패하면 로그에 오류 메시지가 출력되지만 프로그램은 정상 종료됩니다
- 발신 이메일 주소는 자동으로 생성됩니다 (예: nara@localhost)
