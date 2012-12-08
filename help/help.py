#!/usr/bin/python

topics = [
	{
		'title': "What is Priority SMS?",
		'body': '''
			If you've ever missed an important message then Priority SMS is for you! Specify a keyword and Priority SMS will trigger an alarm when a new message arrives containing the keyword. Set the keyword to "emergency" to set off an alarm whenever "emergency" is in the message, or create a secret code to give to your family and close friends. You can even choose to override silent mode to ensure you read the message ASAP.  Whether your phone is in the other room, or if you're sleeping, or whatever the reason may be... you can be sure that Priority SMS won't rest until you're notified of urgent messages.
		''',
	},
	{
		'title': "How can I specify multiple keywords?",
		'body': '''
			Enter your keywords as a comma-separated list.<br /><br />

			e.g. keyword1,keyword2,keyword3<br /><br />

			I hope to make this much easier to use in future updates.
		''',
	},
	{
		'title': "The missed call alarm isn't working",
		'body': '''
			Try increasing the <i>Call Log Delay</i> setting under <i>Advanced Settings</i>. It's set to 2000ms (2s) by default. Increase it to 5000 and see if it works then.
		''',
	},
	{
		'title': "I can't hear the alarm",
		'body': '''
			The sound is sent through the <i>alarm</i> stream, so make sure the volume for your alarms is up. This can be done through your device's settings under <i>Sound</i>, or you can use the volume buttons on your phone while an alarm is playing.
		''',
	},
	{
		'title': "Can I contribute?",
		'body': '''
			You most certainly can! The source can be found on <a href="http://github.com/mattprecious/prioritysms">GitHub</a>. Feel free to fork the repo and submit any changes back to me!
		''',
	},
	{
		'title': "Can you translate Priority SMS to my language?",
		'body': '''
			I can't, but you can! Contribute translations through <a href="http://crowdin.net/project/priority-sms" />Crowdin</a>.
		''',
	},
	{
		'title': "Can I donate?",
		'body': '''
			Yes, yes you can! You can make a donation through PayPal:<br /><br />

			${PAYPAL}
		''',
	},
	{
		'title': "I have a feature request",
		'body': '''
			<a href="/contact">Contact me</a>
		''',
	},
	{
		'title': "These don't solve my problem",
		'body': '''
			<a href="/contact">Contact me</a>. Please describe your problem in as much detail as possible. The more I know about your issue right off the start, the less questions I have to ask you. Also, please provide information about your phone (model, Android version, etc.).
		''',
	},
]

from wheezy.template.engine import Engine
from wheezy.template.ext.core import CoreExtension
from wheezy.template.loader import FileLoader

searchpath = ['.']
engine = Engine(
    loader=FileLoader(searchpath),
    extensions=[CoreExtension()]
)
template = engine.get_template('template.html')

f = open('prioritysms.html', 'w')
f.write(template.render({'pagetitle': 'Priority SMS Help', 'topics': topics}))
f.close()