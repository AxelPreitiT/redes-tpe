FROM node:21

RUN apt-get update && \
    apt-get install -y wget gnupg ca-certificates && \
    wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - && \
    echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list && \
    apt-get update && \
    apt-get install -y google-chrome-stable

RUN wget https://storage.googleapis.com/chrome-for-testing-public/124.0.6367.201/linux64/chromedriver-linux64.zip &&\
    unzip chromedriver-linux64.zip &&\
    mv chromedriver-linux64/chromedriver /usr/bin/
  
RUN curl -sL https://aka.ms/InstallAzureCLIDeb | bash

RUN apt install zip

# Set environment variables
ENV CHROME_BIN=/usr/bin/google-chrome

# Verify installation
RUN google-chrome --version