
pollAjax = (options) ->
	if window.XDomainRequest
		xhr = new XDomainRequest()
	else
		xhr = new XMLHttpRequest()
	xhr.onload = ->
		if @responseText and typeof(@responseText) == "string" and @responseText[0] in ["{", "["]
			responseJson = eval("(#{@responseText})")
			options.callback(responseJson)
		pollAjax(options)
	xhr.open("get", options.url, true)
	if !window.XDomainRequest
		xhr.setRequestHeader("Accept", "application/json; charset=utf-8");
	xhr.send()

getParams = (options) ->
	params = "?ts=#{new Date().getTime()}"
	if options.filters && options.signature
		params += "&filters=#{options.filters}&signature=#{options.signature}"
	params

window.dxes = (baseUrl, appId) ->

	subscribeSSE = (options) ->
		productEvents = new EventSource(baseUrl + "/#{appId}/events/#{options.channel}" + getParams(options))
		productEvents.addEventListener "message", (event) ->
			data = null
			eval("data = #{event.data};")
			options.received(data)

	subscribeAjax = (options) ->
		pollAjax
			url: baseUrl + "/#{appId}/events/#{options.channel}/ajax" + getParams(options)
			callback: (data) ->
				options.received(data)

	subscribeSSE: subscribeSSE
	subscribeAjax: subscribeAjax
	subscribe: (options) ->
		subscribeAjax(options)
		# if !!window["EventSource"]
		# 	try
		# 		subscribeSSE(options)
		# 	catch e
		# 		subscribeJsonp(options)
		# else
		# 	subscribeJsonp(options)

window.dxesInit?()
