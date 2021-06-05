# BMI Bulk Calculator

The repo allows users to calculate BMI when data uploaded in a specific JSON format. 
The expected input is shown below.
Users can submit large JSON files as a job and track it's status.
```
[
  {
    "Gender": "Male",
    "HeightCm": 171,
    "WeightKg": 96
  }
]
```
Once the data is processed users can check the current status of the job.
```
{
  "id": "f2f76133-98a1-4478-a8c2-0d50f68c4ef6",
  "inputFileName": "01-bmi-sample-original.json",
  "completed": true,
  "successful": true,
  "jobSubmittedAt": "2021-06-05T05:41:20.082+00:00",
  "jobStartedAt": "2021-06-05T05:41:20.257+00:00",
  "jobCompletedAt": "2021-06-05T05:41:20.332+00:00",
  "jobLastUpdatedAt": "2021-06-05T05:41:20.334+00:00",
  "noOfRecordsProcessed": 6,
  "noOfRecordsProcessedWithError": 0,
  "reportArtifactLocation": "/path/to/data/out/f2f76133-98a1-4478-a8c2-0d50f68c4ef6.zip",
  "errorMessage": null,
  "errorStackTrace": null
}
```
Once the zip file is extracted, two files are generated.
1. Out file which contains the added fields including BMI, category, health rish fields.
```
[
  {
    "Gender": "Male",
    "HeightCm": 171,
    "WeightKg": 96,
    "BMI": 32.83,
    "Category": "Moderately obese",
    "HealthRisk": "Medium risk"
  },
  {
    "Gender": "Male",
    "HeightCm": 161,
    "WeightKg": 85,
    "BMI": 32.79,
    "Category": "Moderately obese",
    "HealthRisk": "Medium risk"
  },
  {
    "Gender": "Male",
    "HeightCm": 180,
    "WeightKg": 77,
    "BMI": 23.77,
    "Category": "Normal weight",
    "HealthRisk": "Low risk"
  },
  {
    "Gender": "Female",
    "HeightCm": 166,
    "WeightKg": 62,
    "BMI": 22.5,
    "Category": "Normal weight",
    "HealthRisk": "Low risk"
  },
  {
    "Gender": "Female",
    "HeightCm": 150,
    "WeightKg": 70,
    "BMI": 31.11,
    "Category": "Moderately obese",
    "HealthRisk": "Medium risk"
  },
  {
    "Gender": "Female",
    "HeightCm": 167,
    "WeightKg": 82,
    "BMI": 29.4,
    "Category": "Overweight",
    "HealthRisk": "Enhanced risk"
  }
]
```   
2. Summary file contains the summarised information about category, health risk and cumulative of both.
```
{
  "categoryWiseSummary": [
    {
      "category": "Normal weight",
      "count": 2
    },
    {
      "category": "Moderately obese",
      "count": 3
    },
    {
      "category": "Overweight",
      "count": 1
    }
  ],
  "healthRiskWiseSummary": [
    {
      "healthRisk": "Enhanced risk",
      "count": 1
    },
    {
      "healthRisk": "Low risk",
      "count": 2
    },
    {
      "healthRisk": "Medium risk",
      "count": 3
    }
  ],
  "categoryHealthRiskWiseSummary": [
    {
      "category": "Moderately obese",
      "healthRisk": "Medium risk",
      "count": 3
    },
    {
      "category": "Normal weight",
      "healthRisk": "Low risk",
      "count": 2
    },
    {
      "category": "Overweight",
      "healthRisk": "Enhanced risk",
      "count": 1
    }
  ]
}
```
## Steps to build repo using maven

### Pre-requisites
1. JAVA 8
2. Maven 3

### Steps
1. Clone the repo
2. Run maven commands to build and test
```
mvn clean && mvn install && mvn test
```
3. Run maven command
```
mvn spring-boot:run
```
4. Open API page on browser
   http://localhost:8082/swagger-ui.html#

## Steps to build repo using Docker
### Pre-requisites
1. Docker

### Steps
1. Clone the repo
2. Run docker build which includes the test stage
```
docker build -t bmi-calc .
```
3. Run docker run command
```
docker run -it --network=host -p8082:8082 bmi-calc
```
4. Open API page on browser
   http://localhost:8082/swagger-ui.html#
   