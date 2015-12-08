import json
import requests
from pymongo import MongoClient

# read config.json properties
with open('config.json') as json_data_file:
    data = json.load(json_data_file)

# return the database in which all youtube crawled videos are located
def connect_to_db():
    client = MongoClient('localhost', 27017)
    db = client.youtube
    return db


# Database object
db = connect_to_db()

# returns a response to the call of youtube API
def request(query, next_page_token):
    url = (data["api_URL"] + data["part"] + "&q=" + query + "&key=" + data["key"] + "&maxResults=" + data["maxResults"])
    if next_page_token:
        url += "&pageToken=" + next_page_token
    response = requests.get(url)
    print response.url
    return response

# returns the videos object in a single API call
def get_videos(response):
    return response["items"]

# return the next page token
def get_next_page_token(response):
    if "nextPageToken" in response:
        return response["nextPageToken"]
    else:
        return None

# returns true if a link is a video. (to distinguish it from a channel)
def is_video(response, index):
    return "videoId" in response["items"][index]["id"]

# returns the total number of results for a given query
def get_total_results(response):
    return response["pageInfo"]["totalResults"]

# returns a video url
def get_video_url(response, index):
    return response["items"][index]["id"]["videoId"]

# returns a video title
def get_video_title(response,index):
    return response["items"][index]["snippet"]["title"]

# returns a video description
def get_video_description(response, index):
    return response["items"][index]["snippet"]["description"]

# returns a video thumbnail
def get_video_thumbnail(response, index):
    return response["items"][index]["snippet"]["thumbnails"]["medium"]["url"]

# return a response for the first query call
def do_first_request(topic):
    response = request(topic, None)
    return response

# returns a response for the other query call
def do_remaining_requests(query, next_page_token):
    response_JSON = request(query, next_page_token).json()
    return response_JSON

# check i the object is a video, creates a mongoDB docuemnt and inserts it into a Collection called youtube
def elaborate_request(topic, response_JSON):
    next_page_token = get_next_page_token(response_JSON)
    if next_page_token is not None:
        for x in range (0, int(data["maxResults"])):
            if is_video(response_JSON, x) and (db.youtube.find({"videoId": get_video_url(response_JSON, x)}).count() == 0):
                video ={}
                video["topic"] = topic
                video["videoId"] = get_video_url(response_JSON, x)
                video["title"] = get_video_title(response_JSON, x)
                video["description"] = get_video_description(response_JSON, x)
                video["thumbnail"] = get_video_thumbnail(response_JSON, x)
                db.youtube.insert_one(video)
        return next_page_token
    else:
        return None

# actual crawler, does the first request and later calls the next request based on the nextPageToken
for topic in data["topics"]:
    print topic
    print data["topics"][topic]
    for subtopic in data["topics"][topic]:
        r = do_first_request(subtopic)
        response_JSON = r.json()
        next_page_token = get_next_page_token(response_JSON)
        total_results = get_total_results(response_JSON)
        current_page_results = get_videos(response_JSON)
        for x in range(0, int(data["maxYoutubeResults"])/int(data["maxResults"])):
            if elaborate_request(subtopic, response_JSON) is not None:
                next_page_token = elaborate_request(subtopic, response_JSON)
                response_JSON = do_remaining_requests(subtopic, next_page_token)




