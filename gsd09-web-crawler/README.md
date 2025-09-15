# 소개
직접 구현하는 가상 면접 사례로 배우는 대규모 시스템 설계 
웹 크롤러편

kafka topic
`fru1tworld-webcrawling-url-discovery-v1`
message
- url: 현재 url 탐색 경로
- depth: 현재 깊이
- maxDepth: 최대 깊이

api
POST
- `/api/v1/url-discovery`
- 데이터 크롤링 요청
- requestBody 
- - url: 요청 url 주소
- - maxDepth: 최대 깊이 


RDBMS
다운로드 예정 테이블

`crawling_download table`
- id:
- URL: 유일 식별자
- state: READY, ACCEPT, DUPLICATE, COMPLETE
- score: 우선순위 점수
- createdAt:

다운로드 완료 테이블
`crawling_download table`
- id
- URL: 유일 식별자
- HASH: 해시값
- data: 컨텐츠 데이터
